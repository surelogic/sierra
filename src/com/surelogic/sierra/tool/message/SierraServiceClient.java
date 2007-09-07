package com.surelogic.sierra.tool.message;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class SierraServiceClient extends Service {

	/**
	 * Construct a client that points to localhost:8080
	 */
	public SierraServiceClient() {
		super(createUrl(null), new QName(
				"http://services.sierra.surelogic.com/",
				"SierraServiceBeanService"));
	}

	/**
	 * Construct a client pointing to the specified host.
	 * 
	 * @param host
	 *            a String of type <em>server</em> or <em>server</em>:<em>port</em>
	 */
	public SierraServiceClient(SierraServer server) {
		super(createUrl(server), new QName(
				"http://services.sierra.surelogic.com/",
				"SierraServiceBeanService"));
	}

	public SierraServiceClient(URL wsdlDocumentLocation, QName serviceName) {
		super(wsdlDocumentLocation, serviceName);
	}

	/**
	 * Construct a client pointing to the specified hostname. The hostname
	 * should be of the form <em>host</em>[<em>:port</em>].
	 * 
	 * @param server
	 */
	public SierraServiceClient(String server) {
		super(createUrl(new SierraServer(server)), new QName(
				"http://services.sierra.surelogic.com/",
				"SierraServiceBeanService"));
	}

	public SierraService getSierraServicePort() {
		return super.getPort(new QName("http://services.sierra.surelogic.com/",
				"SierraServiceBeanPort"), SierraService.class);
	}

	/**
	 * Create a url that points to the appropriate WSDL document on the target
	 * host.
	 * 
	 * @param host
	 *            a String of type <em>host</em> or <em>host</em>[<em>:port</em>]
	 * @return
	 */
	private static URL createUrl(SierraServer server) {
		String host;
		if (server == null) {
			host = "localhost:8080";
		} else {
			host = server.getHost()
					+ (server.getPort() == null ? "" : (":" + server.getPort()));
		}
		try {
			return new URL("http://" + host
					+ "/SierraServiceBeanService/SierraServiceBean?wsdl");
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

}
