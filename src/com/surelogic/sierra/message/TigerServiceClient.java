package com.surelogic.sierra.message;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;

@WebServiceClient(name = "TigerService", targetNamespace = "http://server.sps.surelogic.com/", wsdlLocation = "http://localhost:8080/TigerServiceBeanService/TigerServiceBean?wsdl")
public class TigerServiceClient extends Service {

	public TigerServiceClient() {
		super(createUrl(), new QName("http://server.sps.surelogic.com/",
				"TigerServiceBeanService"));
	}

	public TigerServiceClient(URL wsdlDocumentLocation, QName serviceName) {
		super(wsdlDocumentLocation, serviceName);
	}

	public TigerService getTigerServicePort() {
		return super.getPort(new QName("http://server.sps.surelogic.com/",
				"TigerServiceBeanPort"), TigerService.class);
	}

	private static URL createUrl() {
		try {
			return new URL(
					"http://localhost:8080/TigerServiceBeanService/TigerServiceBean?wsdl");
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

}
