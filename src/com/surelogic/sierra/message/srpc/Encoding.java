package com.surelogic.sierra.message.srpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.JarTarget;

/**
 * An instance of SRPCEncoding represents the encoding for a particular SPRC
 * interface. This class is thread-safe.
 * 
 * @author nathan
 * 
 */
class Encoding {

	private static final String NULL = "null";
	private static final String GZIP = "application/x-gzip";
	private static final String PLAINTEXT = "text/plain; charset=UTF-8";

	private final JAXBContext context;
	private final Class<?> service;
	private final boolean compressed;

	private Encoding(Class<?> service, String contentType) throws SRPCException {
		this.service = service;
		if (GZIP.equals(contentType)) {
			compressed = true;
		} else if (PLAINTEXT.equals(contentType)) {
			compressed = false;
		} else {
			throw new IllegalArgumentException(contentType
					+ " is not a valid content type");
		}
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(Failure.class);
		classes.add(RaisedException.class);
		// TODO rework this to use MessageWarehouse, or figure out how to
		// properly annotate extension.
		classes.add(FileTarget.class);
		classes.add(JarTarget.class);
		classes.add(FullDirectoryTarget.class);
		classes.add(FilteredDirectoryTarget.class);
		for (Method m : service.getDeclaredMethods()) {
			classes.add(m.getReturnType());
			for (Class<?> clazz : m.getParameterTypes()) {
				classes.add(clazz);
			}
		}
		try {
			this.context = JAXBContext.newInstance(classes
					.toArray(new Class[] {}));
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}
	}

	void encodeMethodInvocation(OutputStream out, MethodInvocation invocation)
			throws SRPCException {
		final PrintWriter writer = wrap(out);
		final Method method = invocation.getMethod();
		final Object[] args = invocation.getArgs();
		writer.println(method.getDeclaringClass().getName());
		writer.println(method.getName());
		if (args != null) {
			if (args.length == 1) {
				if (args[0] != null) {
					try {
						newMarshaller().marshal(args[0], writer);
					} catch (JAXBException e) {
						throw new SRPCException(e);
					}
				}
			} else {
				throw new SRPCException(
						"Messages w/ more than one request parameter are not currently supported.");
			}
		}
		writer.close();
	}

	MethodInvocation decodeMethodInvocation(InputStream in)
			throws SRPCException {
		// Uncompressed, b/c the servlet handles decoding the compression
		final BufferedReader reader = wrap(in);
		try {
			final String clazzStr = reader.readLine();
			if (service.getName().equals(clazzStr)) {
				final String methodStr = reader.readLine();
				for (Method m : service.getDeclaredMethods()) {
					if (m.getName().equals(methodStr)) {
						return new MethodInvocation(m,
								m.getParameterTypes().length == 0 ? null
										: new Object[] { newUnmarshaller()
												.unmarshal(reader) });
					}
				}
				throw new SRPCException("No method found to match " + methodStr
						+ " for service clazzStr");
			} else {
				throw new SRPCException("Invalid service class specified.");
			}
		} catch (IOException e) {
			throw new SRPCException(e);
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}
	}

	void encodeResponse(OutputStream out, ResponseStatus status, Object o)
			throws SRPCException {
		final PrintWriter writer = wrap(out);
		writer.println(status);
		try {
			if (o == null) {
				writer.println(NULL);
			} else {
				writer.println(o.getClass().getName());
				newMarshaller().marshal(o, writer);
			}
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}
		writer.close();
	}

	Object decodeResponse(InputStream in) throws Exception {
		final BufferedReader reader = wrap(in);
		try {
			final String statusStr = reader.readLine();
			if (statusStr != null) {
				ResponseStatus status = null;
				try {
					status = ResponseStatus.valueOf(statusStr);
				} catch (IllegalArgumentException e) {
				}
				if (status != null) {
					final String returnClass = reader.readLine();
					Object value;
					if (NULL.equals(returnClass)) {
						value = null;
					} else {
						value = newUnmarshaller().unmarshal(reader);
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

		} catch (IOException e) {
			throw new SRPCException(e);
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}

	}

	static Encoding getEncoding(Class<?> service, boolean compressed)
			throws SRPCException {
		return new Encoding(service, compressed ? GZIP : PLAINTEXT);
	}

	static Encoding getEncoding(Class<?> service, String contentType)
			throws SRPCException {
		return new Encoding(service, contentType);
	}

	String getCharSet() {
		return "UTF-8";
	}

	String getContentType() {
		return compressed ? GZIP : PLAINTEXT;
	}

	private BufferedReader wrap(InputStream in) {
		if (compressed) {
			try {
				in = new GZIPInputStream(in);
			} catch (IOException e) {
				throw new SRPCException(e);
			}
		}
		return new BufferedReader(new InputStreamReader(in));
	}

	private PrintWriter wrap(OutputStream out) {
		if (compressed) {
			try {
				out = new GZIPOutputStream(out);
			} catch (IOException e) {
				throw new SRPCException(e);
			}
		}
		return new PrintWriter(out);
	}

	private Marshaller newMarshaller() throws SRPCException {
		try {
			return context.createMarshaller();
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}
	}

	private Unmarshaller newUnmarshaller() throws SRPCException {
		try {
			return context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}
	}

}
