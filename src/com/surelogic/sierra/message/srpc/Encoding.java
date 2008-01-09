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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * An instance of SRPCEncoding represents the encoding for a particular SPRC
 * interface. This class is thread-safe.
 * 
 * @author nathan
 * 
 */
class Encoding {

	private final JAXBContext context;
	private final Class<?> service;

	private Encoding(Class<?> service) throws SRPCException {
		this.service = service;
		final Set<Class<?>> classes = new HashSet<Class<?>>();
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
		final PrintWriter writer = new PrintWriter(out);
		final Method method = invocation.getMethod();
		final Object[] args = invocation.getArgs();
		writer.println(method.getDeclaringClass().getName());
		writer.println(method.getName());
		if (args != null) {
			if (args.length == 1) {
				if (args[0] != null) {
					try {
						newMarshaller().marshal(args[0], out);
					} catch (JAXBException e) {
						throw new SRPCException(e);
					}
				}
			} else {
				throw new SRPCException(
						"Messages w/ more than one request parameter are not currently supported.");
			}
		}
		writer.flush();
	}

	MethodInvocation decodeMethodInvocation(InputStream in)
			throws SRPCException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				in));
		try {
			final String clazzStr = reader.readLine();
			if (service.getName().equals(clazzStr)) {
				final String methodStr = reader.readLine();
				for (Method m : service.getDeclaredMethods()) {
					if (m.getName().equals(methodStr)) {
						return new MethodInvocation(m,
								m.getParameterTypes().length == 0 ? null
										: newUnmarshaller().unmarshal(reader));
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
		final PrintWriter writer = new PrintWriter(out);
		writer.println(status);
		try {
			if (o != null) {
				newMarshaller().marshal(o, writer);
			}
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}
		writer.flush();
	}

	Object decodeResponse(InputStream in) throws SRPCException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				in));
		try {
			final String statusStr = reader.readLine();
			if (statusStr != null) {
				ResponseStatus status = null;
				try {
					status = ResponseStatus.valueOf(statusStr);
				} catch (IllegalArgumentException e) {
				}
				if (status != null) {
					return newUnmarshaller().unmarshal(reader);
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

	static Encoding getEncoding(Class<?> service) throws SRPCException {
		return new Encoding(service);
	}

	String getCharSet() {
		return "UTF-8";
	}

	String getContentType() {
		return "text/plain; charset=UTF-8";
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
