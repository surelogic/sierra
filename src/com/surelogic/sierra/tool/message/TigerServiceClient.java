package com.surelogic.sierra.tool.message;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;

@WebServiceClient(name = "TigerService", targetNamespace = "http://services.sierra.surelogic.com/", wsdlLocation = "http://fluid.surelogic.com:13376/TigerServiceBeanService/TigerServiceBean?wsdl")
public class TigerServiceClient extends Service {

	public TigerServiceClient() {
		super(createUrl(), new QName("http://services.sierra.surelogic.com/",
				"TigerServiceBeanService"));
	}

	public TigerServiceClient(URL wsdlDocumentLocation, QName serviceName) {
		super(wsdlDocumentLocation, serviceName);
	}

	public TigerService getTigerServicePort() {
		return super.getPort(new QName("http://services.sierra.surelogic.com/",
				"TigerServiceBeanPort"), TigerService.class);
	}

	private static URL createUrl() {
		try {
			return new URL(
					"http://fluid.surelogic.com:13376/TigerServiceBeanService/TigerServiceBean?wsdl");
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

}
