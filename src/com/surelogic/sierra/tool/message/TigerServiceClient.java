package com.surelogic.sierra.tool.message;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class TigerServiceClient extends Service {

	public TigerServiceClient() {
		super(createUrl(null), new QName(
				"http://services.sierra.surelogic.com/",
				"TigerServiceBeanService"));
	}

	public TigerServiceClient(String host) {
		super(createUrl(host), new QName(
				"http://services.sierra.surelogic.com/",
				"TigerServiceBeanService"));
	}

	public TigerServiceClient(URL wsdlDocumentLocation, QName serviceName) {
		super(wsdlDocumentLocation, serviceName);
	}

	public TigerService getTigerServicePort() {
		return super.getPort(new QName("http://services.sierra.surelogic.com/",
				"TigerServiceBeanPort"), TigerService.class);
	}

	/**
	 * Create a url that points to the appropriate WSDL document on the target
	 * host.
	 * 
	 * @param host
	 *            a String of type <em>host</em> or <em>host</em>:<em>port</em>
	 * @return
	 */
	private static URL createUrl(String host) {
		if (host == null) {
			host = "localhost:8080";
		}
		try {
			return new URL("http://" + host
					+ "/TigerServiceBeanService/TigerServiceBean?wsdl");
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

}
