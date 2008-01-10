package com.surelogic.sierra.tool.message;

import javax.ejb.EJB;

import com.surelogic.sierra.message.srpc.SRPCServlet;
import com.surelogic.sierra.services.SierraServiceLocal;

public class SierraServiceImpl extends SRPCServlet implements SierraService {

	@EJB(beanInterface = SierraServiceLocal.class)
	private SierraService service;

	public CommitAuditTrailResponse commitAuditTrails(
			CommitAuditTrailRequest audits) throws ServerMismatchException {
		return service.commitAuditTrails(audits);
	}

	public AuditTrailResponse getAuditTrails(GetAuditTrailRequest request)
			throws ServerMismatchException {
		return service.getAuditTrails(request);
	}

	public GlobalSettings getGlobalSettings(GlobalSettingsRequest request) {
		return service.getGlobalSettings(request);
	}

	public Qualifiers getQualifiers(QualifierRequest request) {
		return service.getQualifiers(request);
	}

	public SettingsReply getSettings(SettingsRequest request)
			throws ServerMismatchException {
		return service.getSettings(request);
	}

	public ServerUIDReply getUid(ServerUIDRequest request) {
		return service.getUid(request);
	}

	public MergeAuditTrailResponse mergeAuditTrails(MergeAuditTrailRequest seed)
			throws ServerMismatchException {
		return service.mergeAuditTrails(seed);
	}

	public void publishRun(Scan scan) {
		service.publishRun(scan);
	}

	public void writeGlobalSettings(GlobalSettings settings) {
		service.writeGlobalSettings(settings);
	}

}
