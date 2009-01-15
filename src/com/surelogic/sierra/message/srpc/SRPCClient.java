package com.surelogic.sierra.message.srpc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * Handles method dispatch for remotely invokable SRPC services.
 * 
 * @author nathan
 * 
 */
public final class SRPCClient implements InvocationHandler {

	private static final Logger log = SLLogger.getLoggerFor(SRPCClient.class);
	private final HttpClient client;
	private final Encoding codec;
	private final URL url;
	private final Set<Method> methods;

	private SRPCClient(Method[] methods, Encoding codec,
			ServerLocation location, String service) {
		client = new HttpClient();
		final String user = location.getUser();
		if (user != null) {
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(
					new AuthScope(location.getHost(), location.getPort(),
							AuthScope.ANY_REALM),
					new UsernamePasswordCredentials(user, location.getPass()));
		}
		this.methods = new HashSet<Method>(Arrays.asList(methods));
		this.codec = codec;
		this.url = location.createServiceURL(service);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (!methods.contains(method)) {
			// This is a normal method for this object. Pass it along.
			return method.invoke(proxy, args);
		} else {
			log.info("Calling " + method.getName() + " at " + url);
			final PostMethod post = new PostMethod(url.toString());
			final File temp = File.createTempFile("sierra", ".message.gz");
			try {
				final OutputStream out = new BufferedOutputStream(
						new FileOutputStream(temp));
				codec.encodeMethodInvocation(out, new MethodInvocation(method,
						args));
				out.close();
				post.setRequestEntity(new FileRequestEntity(temp, codec
						.getContentType()));
				try {
					client.executeMethod(post);
				} catch (ConnectException e) {
					throw new SierraServiceClientException(
							"Could not connect to " + url, e);
				} catch (UnknownHostException e) {
					throw new SierraServiceClientException(
							"Could not resolve host for " + url, e);
				}
				// TODO these exceptions should be in the right package.
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
				return codec.decodeResponse(post.getResponseBodyAsStream());
			} finally {
				temp.delete();
			}
		}
	}

	/**
	 * Create a remotely invokable instance of the specified interface. By
	 * default, this will attempt to invoke a service under the context root
	 * <code>sierra</code> with the same name as the simple name of the class.
	 * 
	 * @param <T>
	 *            An SRPC interface
	 * @param user
	 * @param password
	 * @param url
	 *            the remote url to dispatch to
	 * @param clazz
	 * @return
	 */
	public static <T> T createClient(ServerLocation location,
			Class<T> clazz, boolean compressed) throws SRPCException {
		if (!Service.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(
					clazz
							+ " must be an implementation of com.surelogic.sierra.message.srpc.Service");
		}
		return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, new SRPCClient(clazz
						.getDeclaredMethods(), Encoding.getEncoding(clazz,
						compressed), location, clazz.getSimpleName())));
	}
}
