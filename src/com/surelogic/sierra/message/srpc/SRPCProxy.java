package com.surelogic.sierra.message.srpc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Handles method dispatch for remotely invokable SRPC services.
 * 
 * @author nathan
 * 
 */
public class SRPCProxy implements InvocationHandler {

	private final HttpClient client;
	private final Encoding codec;
	private final String url;
	private final Set<Method> methods;

	private SRPCProxy(Method[] methods, Encoding codec, String url) {
		client = new HttpClient();
		this.methods = new HashSet<Method>(Arrays.asList(methods));
		this.codec = codec;
		this.url = url;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (!methods.contains(method)) {
			// This is a normal method for this object. Pass it along.
			return method.invoke(proxy, args);
		} else {
			final PostMethod post = new PostMethod(url);
			final File temp = File.createTempFile("sl", "sierra");
			try {
				final GZIPOutputStream zip = new GZIPOutputStream(
						new BufferedOutputStream(new FileOutputStream(temp)));
				codec.encodeMethodInvocation(zip, new MethodInvocation(method,
						args));
				zip.close();
				post.setRequestEntity(new FileRequestEntity(temp, codec
						.getContentType()));
				client.executeMethod(post);
				return codec.decodeResponse(post.getResponseBodyAsStream());
			} finally {
				temp.delete();
			}
		}
	}

	/**
	 * Create a remotely invokable instance of the specified interface.
	 * 
	 * @param <T>
	 *            An SRPC interface
	 * @param url
	 *            the remote url to dispatch to
	 * @param clazz
	 * @return
	 */
	static <T> T createClient(String url, Class<T> clazz) throws SRPCException {
		if (!Service.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(
					clazz
							+ " must be an implementation of com.surelogic.sierra.message.srpc.Service");
		}
		return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, new SRPCProxy(
						clazz.getDeclaredMethods(),
						Encoding.getEncoding(clazz), url)));
	}
}
