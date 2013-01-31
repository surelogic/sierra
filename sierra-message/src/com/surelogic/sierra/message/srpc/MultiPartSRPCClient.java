package com.surelogic.sierra.message.srpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * Handles method dispatch for remotely invokable SRPC services using multi-part
 * post.
 * 
 * @author nathan
 * 
 */
public class MultiPartSRPCClient implements InvocationHandler {

	private static final Logger log = SLLogger
			.getLoggerFor(MultiPartSRPCClient.class);
	private final HttpClient client;
	private final MultiPartEncoding codec;
	private final URL url;
	private final Set<Method> methods;

	private MultiPartSRPCClient(final Method[] methods,
			final MultiPartEncoding codec, final ServerLocation location,
			final String service) {
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

	@Override
  public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		if (!methods.contains(method)) {
			// This is a normal method for this object. Pass it along.
			return method.invoke(proxy, args);
		} else {
			log.info("Calling " + method.getName() + " at " + url);
			return codec.postMethodInvocation(client, url,
					new MethodInvocation(method, args));
		}
	}

	/**
	 * Create a remotely invokable instance of the specified interface. By
	 * default, this will attempt to invoke a service under the context root
	 * <code>sierra</code> with the same name as the simple name of the class.
	 * 
	 * @param <T>
	 *            An SRPC interface
	 * @param location
	 *            the location of the server this client will always connect to
	 * @param clazz
	 *            a valid {@link Service} interface
	 * @param compressed
	 *            whether or not to gzip all messages before sending and
	 *            receiving them
	 * @return
	 */
	public static <T> T createClient(final ServerLocation location,
			final Class<T> clazz, final boolean compressed)
			throws SRPCException {

		return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, new MultiPartSRPCClient(clazz
						.getDeclaredMethods(), MultiPartEncoding.getEncoding(
						clazz, compressed), location, clazz.getSimpleName())));
	}
}
