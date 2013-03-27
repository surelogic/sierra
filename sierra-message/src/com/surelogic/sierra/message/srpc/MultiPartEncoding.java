package com.surelogic.sierra.message.srpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import com.surelogic.sierra.tool.message.InvalidVersionException;
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
	private static final String CHARSET = "UTF-8";
	private final JAXBContext context;
	private final Class<?> service;
	private final boolean compressed;
	private final String serviceVersion;

	private MultiPartEncoding(final Class<?> service, final boolean compressed) {
		this.service = service;
		this.compressed = compressed;
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(Failure.class);
		classes.add(RaisedException.class);
		classes.add(InvalidVersion.class);
		// TODO rework this to use MessageWarehouse, or figure out how to
		// properly annotate extension.
		classes.add(FileTarget.class);
		classes.add(JarTarget.class);
		classes.add(FullDirectoryTarget.class);
		classes.add(FilteredDirectoryTarget.class);
		final Service ann = service.getAnnotation(Service.class);
		if (ann == null) {
			throw new IllegalArgumentException(
					service
							+ " must be annotated with com.surelogic.sierra.message.srpc.Service");
		}
		serviceVersion = ann.version();
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
	 * @param version
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
			final Part[] parts = new Part[classes.length + 2];
			final List<File> temps = new ArrayList<File>();
			final Marshaller m;
			try {
				m = context.createMarshaller();
			} catch (final JAXBException e) {
				throw new SRPCException(e);
			}
			parts[0] = new StringPart("version", serviceVersion, CHARSET);
			parts[1] = new StringPart("method", invocation.getMethod()
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
						parts[i + 2] = new FilePart(Integer.toString(i), tmp);
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
			if (e instanceof InvalidVersionException) {
				throw (InvalidVersionException) e;
			} else if (e instanceof InvalidLoginException) {
				throw (InvalidLoginException) e;
			} else if (e instanceof ServiceInvocationException) {
				throw (ServiceInvocationException) e;
			}
			throw new SRPCException(e);
		}
	}

	/**
	 * Decode a method invocation from the given request. Performs version
	 * checking to make sure the client protocol is compatible with this
	 * protocol.
	 * 
	 * @param req
	 * @return
	 * @throws SRPCException
	 *             if something goes wrong
	 * @throws InvalidVersionException
	 *             if the request uses a different protocol version than this
	 *             service
	 */
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
			String version = null;
			for (final FileItem item : items) {
				if (item.isFormField() && "version".equals(item.getFieldName())) {
					version = item.getString();
					break;
				}
			}
			if (version == null) {
				throw new SRPCException(
						"No version was associated with the request");
			} else if (!serviceVersion.equals(version)) {
				throw new InvalidVersionException(serviceVersion, version);
			}
			String methodName = null;
			for (final FileItem item : items) {
				if (item.isFormField() && "method".equals(item.getFieldName())) {
					methodName = item.getString();
					break;
				}
			}
			if (methodName == null) {
				throw new SRPCException(
						"No method name was specified on a request.");
			}
			for (final Method m : service.getMethods()) {
				if (methodName.equals(m.getName())) {
					final Class<?>[] params = m.getParameterTypes();
					final Object[] args = new Object[params.length];
					try {
						final Unmarshaller um = context.createUnmarshaller();
						for (final FileItem item : items) {
							if (!item.isFormField()) {
								try {
									final int i = Integer.parseInt(item
											.getFieldName());
									if (File.class.isAssignableFrom(params[i])) {
										final File file = File.createTempFile(
												"sierra", "tmp");
										item.write(file);
										args[i] = file;
									} else {
										final InputStream in = wrap(item
												.getInputStream());
										try {
											args[i] = um.unmarshal(in);
										} finally {
											in.close();
										}
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
		} catch (final FileUploadException e) {
			throw new SRPCException(e);
		}
	}

	void encodeResponse(OutputStream out, final ResponseStatus status,
			final Object o, final Class<?> type) throws SRPCException {
		try {
			out = wrap(out);
			try {
				out.write(status.getByte());
				try {
					if (File.class.isAssignableFrom(type)) {
						out.write(Type.FILE.getByte());
						writeFileContents((File) o, out);
					} else if (o == null) {
						out.write(Type.NULL.getByte());
					} else {
						out.write(Type.XML.getByte());
						context.createMarshaller().marshal(o, out);
					}
				} catch (final JAXBException e) {
					throw new SRPCException(e);
				}
			} finally {
				out.close();
			}
		} catch (final IOException e) {
			throw new SRPCException(e);
		}

	}

	Object decodeResponse(InputStream in, final Class<?> returnType)
			throws Exception {
		try {
			in = wrap(in);
			final ResponseStatus status = ResponseStatus.fromByte((byte) in
					.read());
			final Type t = Type.fromByte((byte) in.read());
			Object value;
			switch (t) {
			case NULL:
				value = null;
				break;
			case FILE:
				value = File.createTempFile("sierra", "tmp");
				writeStreamContents(in, (File) value);
				break;
			case XML:
				value = context.createUnmarshaller().unmarshal(in);
				break;
			default:
				throw new IllegalStateException("Unknown response status.");
			}
			switch (status) {
			case OK:
				return value;
			case RAISED:
				final RaisedException e = (RaisedException) value;
				throw e.regenerateException();
			case FAIL:
				final Failure failure = (Failure) value;
				throw new ServiceInvocationException(failure.getMessage()
						+ "\n" + failure.getTrace());
			case VERSION:
				final InvalidVersion iv = (InvalidVersion) value;
				final InvalidVersionException exc = new InvalidVersionException(
						iv.getServerVersion(), iv.getClientVersion());
				throw exc;
			default:
				throw new IllegalStateException("Unknown response type.");
			}
		} catch (final IOException e) {
			throw new SRPCException(e);
		} catch (final JAXBException e) {
			throw new SRPCException(e);
		} finally {
			in.close();
		}
	}

	private static void writeFileContents(final File f, final OutputStream out)
			throws IOException {
		final InputStream in = new BufferedInputStream(new FileInputStream(f));
		try {
			final byte[] buf = new byte[4096];
			int read = 0;
			while ((read = in.read(buf, 0, 4096)) > 0) {
				out.write(buf, 0, read);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	private static void writeStreamContents(final InputStream in, final File f)
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
		return getEncoding(service, true);
	}

	enum Type {
		FILE((byte) 'f'), NULL((byte) 'n'), XML((byte) 'x');

		private final byte f_byte;

		Type(final byte b) {
			f_byte = b;
		}

		byte getByte() {
			return f_byte;
		}

		public static Type fromByte(final byte b) {
			for (final Type s : values()) {
				if (b == s.getByte()) {
					return s;
				}
			}
			throw new IllegalArgumentException("Invalid byte.");
		}
	}

}
