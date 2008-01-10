package com.surelogic.sierra.message.srpc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

import com.surelogic.sierra.tool.message.SierraServerLocation;

/**
 * Handles method dispatch for remotely invokable SRPC services.
 * 
 * @author nathan
 * 
 */
public class SRPCClient implements InvocationHandler {

	private final HttpClient client;
	private final Encoding codec;
	private final URL url;
	private final Set<Method> methods;

	private SRPCClient(Method[] methods, Encoding codec,
			SierraServerLocation location, String service) {
		client = new HttpClient();
		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(
				new AuthScope(location.getHost(), location.getPort(),
						AuthScope.ANY_REALM),
				new UsernamePasswordCredentials(location.getUser(), location
						.getPass()));
		this.methods = new HashSet<Method>(Arrays.asList(methods));
		this.codec = codec;
		this.url = location.createServiceUrl(service);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (!methods.contains(method)) {
			// This is a normal method for this object. Pass it along.
			return method.invoke(proxy, args);
		} else {
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
				client.executeMethod(post);
				if(post.getStatusCode() == 401) {
					throw new InvalidAuthenticationException(post.getStatusCode() + ":" + post.getStatusText());
				}
				if (post.getStatusCode() != 200) {
					throw new InvalidServiceException(
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
	public static <T> T createClient(SierraServerLocation location,
			Class<T> clazz) throws SRPCException {
		if (!Service.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(
					clazz
							+ " must be an implementation of com.surelogic.sierra.message.srpc.Service");
		}
		return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, new SRPCClient(clazz
						.getDeclaredMethods(), Encoding.getEncoding(clazz),
						location, clazz.getSimpleName())));
	}
}
