package com.surelogic.sierra.message.srpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.zip.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.surelogic.common.FileUtility;
import com.surelogic.common.logging.SLLogger;
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
	private static final boolean allowMultipleArgs = false;
	private static final String RECORD_MESSAGES_PROP = "com.surelogic.recordMessages";
	private static final Logger log = SLLogger.getLoggerFor(Encoding.class);
	private static final Class<?>[] NO_CLASSES = new Class[0];
	private static final String NULL = "null";
	private static final String GZIP = "application/x-gzip";
	private static final String PLAINTEXT = "text/plain; charset=UTF-8";
	private static final AtomicInteger methodCount = new AtomicInteger();
	private static final Date start = new Date();
	private final JAXBContext context;
	private final Class<?> service;
	private final boolean compressed;
	private final boolean recordMessages;

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
			this.context = JAXBContext.newInstance(classes.toArray(NO_CLASSES));
		} catch (JAXBException e) {
			throw new SRPCException(e);
		}
		this.recordMessages = "true".equalsIgnoreCase(System
				.getProperty(RECORD_MESSAGES_PROP));
	}

	void encodeMethodInvocation(OutputStream out, MethodInvocation invocation)
			throws SRPCException {
		encodeMethodInvocationHelper(out, invocation);
		if (recordMessages) {
			try {
				final int count = methodCount.incrementAndGet();
				OutputStream file = new FileOutputStream(FileUtility
						.getSierraDataDirectory()
						+ File.separator
						+ invocation.getMethod().getName()
						+ "."
						+ timestamp()
						+ "."
						+ count
						+ (compressed ? ".gz" : ".txt"));
				try {
					encodeMethodInvocationHelper(file, invocation);
				} finally {
					file.close();
				}
			} catch (FileNotFoundException e) {
				log.warning("Could not record request: " + e.getMessage());
			} catch (IOException e) {
				log.warning("Could not record request: " + e.getMessage());
			}
		}
	}

	void encodeMethodInvocationHelper(OutputStream out,
			MethodInvocation invocation) throws SRPCException {
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
				throw new SRPCException("Invalid service class specified: "+clazzStr);
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
					if (recordMessages) {
						PrintWriter out = wrap(new FileOutputStream(FileUtility
								.getSierraDataDirectory()
								+ File.separator
								+ "response."
								+ timestamp()
								+ "."
								+ methodCount
								+ (compressed ? ".gz" : ".txt")));
						try {
							if (value == null) {
								out.write("null\n");
							} else {
								newMarshaller().marshal(value, out);
							}
						} finally {
							out.close();
						}
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
		return getEncoding(service, compressed ? GZIP : PLAINTEXT);
	}

	static Encoding getEncoding(Class<?> service, String contentType)
			throws SRPCException {
		if (allowMultipleArgs) {
			return new ZipEncoding(service, contentType);
		}
		return new Encoding(service, contentType);
	}

	String getCharSet() {
		return "UTF-8";
	}

	String getContentType() {
		return compressed ? GZIP : PLAINTEXT;
	}

	private InputStream wrapStream(InputStream in) {
		if (compressed) {
			try {
				return new GZIPInputStream(in);
			} catch (IOException e) {
				throw new SRPCException(e);
			}
		}
		return in;
	}
	
	private BufferedReader wrap(InputStream in) {
		in = wrapStream(in);
		return new BufferedReader(new InputStreamReader(in));
	}

	private OutputStream wrapStream(OutputStream out) {
		if (compressed) {
			try {
				return new GZIPOutputStream(out);
			} catch (IOException e) {
				throw new SRPCException(e);
			}
		}
		return out;
	}
	
	private PrintWriter wrap(OutputStream out) {
		out = wrapStream(out);
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

	private static String timestamp() {
		return Long.toString(start.getTime() / 1000);
	}

	static void encodeFile(OutputStream out, File f) throws IOException {
		FileInputStream fin    = new FileInputStream(f);
		BufferedInputStream in = new BufferedInputStream(fin);
		copyFileContents(in, out);
	}
	
	static void copyFileContents(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[4096];
		int read   = 0;
		while ((read = in.read(buf, 0, 4096)) > 0) {
			out.write(buf, 0, read);
		}
		out.flush();
	}
		
	static File decodeFile(String method, String suffix, InputStream in) throws IOException {
		// FIX name doesn't match original???
		File temp                = File.createTempFile(method, ".tmp"); 
		FileOutputStream fout    = new FileOutputStream(temp);
		BufferedOutputStream out = new BufferedOutputStream(fout);
		copyFileContents(in, out);
		return temp;
	}
	
	private interface FileHandler {
		void handleFile(int i) throws Exception;
		void handleVarargsFile(int i) throws Exception;
		void handleOther(int i) throws Exception;
	}
	
	private static final File[] noFiles = new File[0];
	
	private static class ZipEncoding extends Encoding {
		ZipEncoding(Class<?> service, String contentType) throws SRPCException {
			super(service, contentType);
		}
		
		private void createEntry(ZipOutputStream zout, String name) throws IOException {
			ZipEntry ze = new ZipEntry(name);				
			zout.putNextEntry(ze);
		}
		
		private void handleParameters(Method method, FileHandler h) throws Exception {
			final Class<?>[] args = method.getParameterTypes();		
			for(int i=0; i<args.length; i++) {
				final boolean last = (i == args.length-1);
				if (args[i] == File.class) {
					h.handleFile(i);
				}
				else if (last && args[i].isArray() && args[i].getComponentType() == File.class) {
					h.handleVarargsFile(i);
				}
				else {
					h.handleOther(i);
				}
			}
		}
		
		@Override
		void encodeMethodInvocationHelper(OutputStream out,
				MethodInvocation invocation) {			
			final ZipOutputStream zout = new ZipOutputStream(super.wrapStream(out));
			final Method method = invocation.getMethod();
			final Object[] args = invocation.getArgs();
			try {
				// Using separate entries, since comments don't seem to be transmitted
				createEntry(zout, method.getDeclaringClass().getName());				
				createEntry(zout, method.getName());				
				
				handleParameters(method, new FileHandler() {
					private void setup(int i, int j) throws IOException {
						final String name;
						if (j < 0) {
							name = args[i].getClass().getName()+"."+i;
						} else {
							name = args[i].getClass().getName()+"."+i+"_"+j;
						}
						createEntry(zout, name);
					}					
					public void handleFile(int i) throws Exception {
						setup(i, -1);
						encodeFile(zout, (File) args[i]);
						zout.closeEntry();
					}
					public void handleOther(int i) throws Exception {
						setup(i, -1);
						ZipEncoding.super.newMarshaller().marshal(args[i], zout);
						zout.closeEntry();
					}
					public void handleVarargsFile(int i) throws Exception {
						File[] files = (File[]) args[i];						
						if (files == null) {
							return;
						}
						for(int j=0; j<files.length; j++) {
							setup(i, j);
							encodeFile(zout, files[j]);							
							zout.closeEntry();
						}
					}					
				});
				zout.close();
			} catch (Exception e) {
				throw new SRPCException(e);
			}
		}
		
		@Override
		MethodInvocation decodeMethodInvocation(InputStream in)
		throws SRPCException {
			File temp = null;
			try {
				// Uncompressed, b/c the servlet handles decoding the compression
				in   = super.wrapStream(in);			
				temp = decodeFile("Encoding", ".zip", in);		
				// Saved to a file, so I can get separate streams for each arg
				// to workaround an apparent close() by unmarshalling
				final ZipFile zip                         = new ZipFile(temp);				
				final Enumeration<? extends ZipEntry> zen = zip.entries();
				
				ZipEntry ze            = zen.nextElement();
				final String clazzStr  = ze.getName();
				final String methodStr = zen.nextElement().getName();
				if (super.service.getName().equals(clazzStr)) {					
					for (final Method m : super.service.getDeclaredMethods()) {						
						if (m.getName().equals(methodStr)) {					
							final Object[] args;
							final int numArgs = m.getParameterTypes().length;
							
							if (numArgs > 0) {
								args = new Object[numArgs];
								handleParameters(m, new FileHandler() {
									public void handleFile(int i) throws Exception {
										ZipEntry ze = zen.nextElement();
										args[i] = decodeFile(m.getName(), ".tmp", zip.getInputStream(ze));
									}
									public void handleVarargsFile(int i) throws Exception {
										if (zen.hasMoreElements()) {
											List<File> files = new ArrayList<File>();
											while (zen.hasMoreElements()) {
												ZipEntry ze = zen.nextElement();
												files.add(decodeFile(m.getName(), ".tmp", zip.getInputStream(ze)));	
											}
											args[i] = files.toArray(noFiles);
										} else {
											args[i] = noFiles;
										} 									
									}
									public void handleOther(int i) throws Exception {
										ZipEntry ze = zen.nextElement();
										args[i] = ZipEncoding.super.newUnmarshaller().unmarshal(zip.getInputStream(ze));
									}									
								});								
							} else {
								args = null;
							}
							zip.close();
							return new MethodInvocation(m, args);
						}
					}
					throw new SRPCException("No method found to match " + methodStr
							+ " for service clazzStr");
				} else {
					throw new SRPCException("Invalid service class specified: "+clazzStr+"."+methodStr);
				}
			} catch (Exception e) {
				throw new SRPCException(e);
			} finally {
				if (temp != null && temp.exists()) {
					temp.delete();
				}
			}
		}
	}
}
