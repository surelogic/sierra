package com.surelogic.sierra.tool.message;

import java.net.Authenticator;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class SierraServiceClient extends Service {

	/**
	 * Construct a client that points to localhost:8080
	 */
	public SierraServiceClient() {
		super(SierraServerLocation.DEFAULT.createWSDLUrl(), new QName(
				"http://services.sierra.surelogic.com/",
				"SierraServiceBeanService"));
	}

	/**
	 * Construct a client pointing to the specified host.
	 * 
	 * @param host
	 *            a String of type <em>server</em> or <em>server</em>:<em>port</em>
	 */
	public SierraServiceClient(SierraServerLocation server) {
		super(server == null ? SierraServerLocation.DEFAULT.createWSDLUrl()
				: server.createWSDLUrl(), new QName(
				"http://services.sierra.surelogic.com/",
				"SierraServiceBeanService"));

		// I need this for BASIC HTTP authenticator for connecting to the
		// WebService
		Authenticator.setDefault(new SierraServiceClientAuthenticator(server
				.getUser(), server.getPass()));
	}

	public SierraServiceClient(URL wsdlDocumentLocation, QName serviceName) {
		super(wsdlDocumentLocation, serviceName);
	}

	public SierraService getSierraServicePort() {
		return super.getPort(new QName("http://services.sierra.surelogic.com/",
				"SierraServiceBeanPort"), SierraService.class);
	}

}
