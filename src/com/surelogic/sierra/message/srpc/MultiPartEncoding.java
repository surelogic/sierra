package com.surelogic.sierra.message.srpc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.SierraServiceClientException;
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.JarTarget;

/**
 * An instance of MultiPartEncoding implements the encoding for a particular
 * SRPC interface as a multi-part POST.
 * 
 * @author nathan
 * 
 */
class MultiPartEncoding {

	private static final Logger log = SLLogger
			.getLoggerFor(MultiPartEncoding.class);
	private static final Class<?>[] NO_CLASSES = new Class[0];
	private static final String NULL = "null";
	private static final String GZIP = "application/x-gzip";
	private static final String PLAINTEXT = "text/plain; charset=UTF-8";
	private static final String CHARSET = "UTF-8";
	private final JAXBContext context;
	private final Class<?> service;
	private final boolean compressed;

	private MultiPartEncoding(final Class<?> service, final boolean compressed) {
		this.service = service;
		this.compressed = compressed;
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(Failure.class);
		classes.add(RaisedException.class);
		// TODO rework this to use MessageWarehouse, or figure out how to
		// properly annotate extension.
		classes.add(FileTarget.class);
		classes.add(JarTarget.class);
		classes.add(FullDirectoryTarget.class);
		classes.add(FilteredDirectoryTarget.class);
		for (final Method m : service.getDeclaredMethods()) {
			classes.add(m.getReturnType());
			for (final Class<?> clazz : m.getParameterTypes()) {
				classes.add(clazz);
			}
		}
		try {
			this.context = JAXBContext.newInstance(classes.toArray(NO_CLASSES));
		} catch (final JAXBException e) {
			throw new SRPCException(e);
		}
	}

	/**
	 * Makes a multi-part post using the provided {@link HttpClient}, and return
	 * the decoded response from the server.
	 * 
	 * @param client
	 * @param invocation
	 * @return
	 */
	Object postMethodInvocation(final HttpClient client, final URL url,
			final MethodInvocation invocation) {
		try {
			final PostMethod post = new PostMethod(url.toString());
			final Class<?>[] classes = invocation.getMethod()
					.getParameterTypes();
			final Object[] args = invocation.getArgs();
			final Part[] parts = new Part[classes.length + 1];
			final List<File> temps = new ArrayList<File>();
			final Marshaller m;
			try {
				m = context.createMarshaller();
			} catch (final JAXBException e) {
				throw new SRPCException(e);
			}
			parts[0] = new StringPart("method", invocation.getMethod()
					.getName(), CHARSET);
			for (int i = 0; i < classes.length; i++) {
				if (File.class.isAssignableFrom(classes[i])) {
					parts[i] = new FilePart(Integer.toString(i), (File) args[i]);
				} else {
					try {
						final File tmp = File.createTempFile("sierra", "tmp");
						temps.add(tmp);
						final OutputStream o = wrap(new FileOutputStream(tmp));
						try {
							m.marshal(args[i], o);
						} finally {
							o.close();
						}
						parts[i + 1] = new FilePart(Integer.toString(i), tmp);
					} catch (final IOException e) {
						throw new SRPCException(e);
					}
				}
			}
			post.setRequestEntity(new MultipartRequestEntity(parts, post
					.getParams()));
			try {
				client.executeMethod(post);
			} catch (final ConnectException e) {
				throw new SierraServiceClientException("Could not connect to "
						+ url, e);
			} catch (final UnknownHostException e) {
				throw new SierraServiceClientException(
						"Could not resolve host for " + url, e);
			} finally {
				for (final File f : temps) {
					f.delete();
				}
			}
			if (post.getStatusCode() == 401) {
				throw new InvalidLoginException(post.getStatusCode() + ":"
						+ post.getStatusText());
			}
			if (post.getStatusCode() != 200) {
				throw new SierraServiceClientException(
						"Problem locating service at " + url + ": Status: "
								+ post.getStatusCode() + ": "
								+ post.getStatusText());
			}
			return decodeResponse(post.getResponseBodyAsStream(), invocation
					.getMethod().getReturnType());

		} catch (final Exception e) {
			throw new SRPCException(e);
		}
	}

	@SuppressWarnings("unchecked")
	MethodInvocation decodeMethodInvocation(final HttpServletRequest req) {
		if (!ServletFileUpload.isMultipartContent(req)) {
			log.info("Non multi-part content request made to servlet");
			return null;
		}
		final DiskFileItemFactory factory = new DiskFileItemFactory();
		final ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			final List<FileItem> items = upload.parseRequest(req);
			String methodName = null;
			for (final FileItem item : items) {
				if (item.isFormField()) {
					if ("method".equals(item.getFieldName())) {
						methodName = item.getString();
						break;
					}
				}
			}
			if (methodName != null) {
				for (final Method m : service.getMethods()) {
					if (methodName.equals(m.getName())) {
						final Class<?>[] params = m.getParameterTypes();
						final Object[] args = new Object[params.length];
						try {
							final Unmarshaller um = context
									.createUnmarshaller();
							for (final FileItem item : items) {
								if (!item.isFormField()) {
									try {
										final int i = Integer.parseInt(item
												.getFieldName());
										if (params[i]
												.isAssignableFrom(File.class)) {
											final File file = File
													.createTempFile("sierra",
															"tmp");
											item.write(file);
											args[i] = file;
										} else {
											args[i] = um.unmarshal(item
													.getInputStream());
										}
									} catch (final NumberFormatException e) {
										// Not a recognized form element, we'll
										// just ignore it
									}
								}
							}
							return new MethodInvocation(m, args);
						} catch (final Exception e) {
							throw new SRPCException(e);
						}
					}
				}
				throw new SRPCException(methodName
						+ " is not a valid method on this service");
			} else {
				throw new SRPCException(
						"No method name was specified on a request.");
			}
		} catch (final FileUploadException e) {
			throw new SRPCException(e);
		}
	}

	void encodeResponse(final OutputStream out, final ResponseStatus status,
			final Object o) throws SRPCException {
		try {
			final PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					wrap(out)));
			writer.println(status);
			try {
				if (o == null) {
					writer.println(NULL);
				} else {
					writer.println(o.getClass().getName());
					context.createMarshaller().marshal(o, writer);
				}
			} catch (final JAXBException e) {
				throw new SRPCException(e);
			}
			writer.close();
		} catch (final IOException e) {
			throw new SRPCException(e);
		}

	}

	Object decodeResponse(final InputStream in, final Class<?> returnType)
			throws Exception {
		if (File.class.isAssignableFrom(returnType)) {
			final File f = File.createTempFile("sierra", "tmp");
			writeStreamContents(in, f);
			return f;
		}
		// FIXME
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				wrap(in)));
		try {
			final String statusStr = reader.readLine();
			if (statusStr != null) {
				ResponseStatus status = null;
				try {
					status = ResponseStatus.valueOf(statusStr);
				} catch (final IllegalArgumentException e) {
				}
				if (status != null) {
					final String returnClass = reader.readLine();
					Object value;
					if (NULL.equals(returnClass)) {
						value = null;
					} else {
						value = context.createUnmarshaller().unmarshal(reader);
					}
					switch (status) {
					case OK:
						return value;
					case RAISED:
						final RaisedException e = (RaisedException) value;
						throw e.regenerateException();
					case FAIL:
						final Failure failure = (Failure) value;
						throw new ServiceInvocationException(failure
								.getMessage()
								+ "\n" + failure.getTrace());
					default:
						break;
					}

				}
			}
			throw new SRPCException(
					"Response did not begin with an acceptable status code.  The first line was: "
							+ statusStr);

		} catch (final IOException e) {
			throw new SRPCException(e);
		} catch (final JAXBException e) {
			throw new SRPCException(e);
		}

	}

	static void writeStreamContents(final InputStream in, final File f)
			throws IOException {
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(
				f));
		final byte[] buf = new byte[4096];
		int read = 0;
		while ((read = in.read(buf, 0, 4096)) > 0) {
			out.write(buf, 0, read);
		}
		out.close();
	}

	OutputStream wrap(final OutputStream o) throws IOException {
		return compressed ? new GZIPOutputStream(o) : o;
	}

	InputStream wrap(final InputStream i) throws IOException {
		return compressed ? new GZIPInputStream(i) : i;
	}

	static MultiPartEncoding getEncoding(final Class<?> service,
			final boolean compressed) throws SRPCException {
		return new MultiPartEncoding(service, compressed);
	}

	static MultiPartEncoding getEncoding(final Class<?> service)
			throws SRPCException {
		return getEncoding(service, false);
	}

}
