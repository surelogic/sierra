package com.surelogic.sierra.tool.message.jaxws;

import com.surelogic.sierra.tool.message.AuditTrailResponse;
import com.surelogic.sierra.tool.message.CommitAuditTrailRequest;
import com.surelogic.sierra.tool.message.CommitAuditTrailResponse;
import com.surelogic.sierra.tool.message.GetAuditTrailRequest;
import com.surelogic.sierra.tool.message.GlobalSettings;
import com.surelogic.sierra.tool.message.GlobalSettingsRequest;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.MergeAuditTrailRequest;
import com.surelogic.sierra.tool.message.MergeAuditTrailResponse;
import com.surelogic.sierra.tool.message.QualifierRequest;
import com.surelogic.sierra.tool.message.Qualifiers;
import com.surelogic.sierra.tool.message.Scan;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.ServerUIDReply;
import com.surelogic.sierra.tool.message.ServerUIDRequest;
import com.surelogic.sierra.tool.message.SettingsReply;
import com.surelogic.sierra.tool.message.SettingsRequest;
import com.surelogic.sierra.tool.message.SierraServerLocation;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClientAuthenticator;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

import java.net.Authenticator;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

public class JAXWSClient extends Service implements SierraService {

	private final SierraService service;

	/**
	 * Construct a client that points to localhost:8080
	 */
	public JAXWSClient() {
		super(SierraServerLocation.DEFAULT.createWSDLUrl(), new QName(
				"http://services.sierra.surelogic.com/",
				"SierraServiceBeanService"));
		this.service = getSierraServicePort();
	}

	/**
	 * Construct a client pointing to the specified host.
	 * 
	 * @param host
	 *            a String of type <em>server</em> or <em>server</em>:<em>port</em>
	 */
	public JAXWSClient(SierraServerLocation server) {
		super((server == null) ? SierraServerLocation.DEFAULT.createWSDLUrl()
				: server.createWSDLUrl(), new QName(
				"http://services.sierra.surelogic.com/",
				"SierraServiceBeanService"));

		// I need this for BASIC HTTP authenticator for connecting to the
		// WebService
		Authenticator.setDefault(new SierraServiceClientAuthenticator(server
				.getUser(), server.getPass()));
		this.service = getSierraServicePort();
	}

	public JAXWSClient(URL wsdlDocumentLocation, QName serviceName) {
		super(wsdlDocumentLocation, serviceName);
		this.service = getSierraServicePort();
	}

	private SierraService getSierraServicePort() {
		return super.getPort(new QName("http://services.sierra.surelogic.com/",
				"SierraServiceBeanPort"), SierraService.class);
	}

	public CommitAuditTrailResponse commitAuditTrails(
			CommitAuditTrailRequest audits) throws ServerMismatchException {
		try {
			return service.commitAuditTrails(audits);
		} catch (WebServiceException e) {
			throw handleException(e);
		}

	}

	public AuditTrailResponse getAuditTrails(GetAuditTrailRequest request)
			throws ServerMismatchException {
		try {
			return service.getAuditTrails(request);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	public GlobalSettings getGlobalSettings(GlobalSettingsRequest request) {
		try {
			return service.getGlobalSettings(request);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	public Qualifiers getQualifiers(QualifierRequest request) {
		try {
			return service.getQualifiers(request);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	public SettingsReply getSettings(SettingsRequest request)
			throws ServerMismatchException {
		try {
			return service.getSettings(request);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	public ServerUIDReply getUid(ServerUIDRequest request) {
		try {
			return service.getUid(request);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	public MergeAuditTrailResponse mergeAuditTrails(MergeAuditTrailRequest seed)
			throws ServerMismatchException {
		try {
			return service.mergeAuditTrails(seed);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	public void publishRun(Scan scan) {
		try {
			service.publishRun(scan);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	public void writeGlobalSettings(GlobalSettings settings) {
		try {
			service.writeGlobalSettings(settings);
		} catch (WebServiceException e) {
			throw handleException(e);
		}
	}

	private SierraServiceClientException handleException(WebServiceException e) {
		if (e.getMessage().startsWith(
				"request requires HTTP authentication: Unauthorized")) {
			return new InvalidLoginException(e);
		} else {
			return new SierraServiceClientException(e);
		}
	}
}
