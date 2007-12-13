package com.surelogic.sierra.tool.message;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

@WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.BARE)
public interface SierraService {
	/**
	 * Publish a run to the server.
	 * 
	 * @param run
	 * @return whether or not the run was successfully generated on the server.
	 */
	void publishRun(@WebParam(name = "scan")
	Scan scan);

	/**
	 * Get the set of available qualifier names.
	 * 
	 * @return
	 */
	Qualifiers getQualifiers(QualifierRequest request);

	/**
	 * Return the server's unique identifier
	 * 
	 * @return
	 */
	ServerUIDReply getUid(ServerUIDRequest request);

	/**
	 * Return the uid of an audit trail that matches the given seed. If none
	 * matches, a new audit trail will be created.
	 * 
	 * @param seed
	 * @return the unique identifier of a global audit trail.
	 */
	MergeAuditTrailResponse mergeAuditTrails(MergeAuditTrailRequest seed)
			throws ServerMismatchException;

	/**
	 * Commit an audit trail of transactions. The audit trail consists of any
	 * new match locations that have been added to the trail, as well as any new
	 * auditing events.
	 * 
	 * @param audits
	 * @return the server revision containing the new audits
	 */
	CommitAuditTrailResponse commitAuditTrails(CommitAuditTrailRequest audits)
			throws ServerMismatchException;

	/**
	 * Get all of the auditing events for the given qualifier and project
	 * occurring after the specified revision.
	 * 
	 * @param request
	 * @return all audit trails and the audits they contain after the specified
	 *         revision
	 */
	AuditTrailResponse getAuditTrails(GetAuditTrailRequest request)
			throws ServerMismatchException;

	/**
	 * Return the settings associated with a given qualifier.
	 * 
	 * @param request
	 * @return the settings associated with a given qualifier.
	 */
	SettingsReply getSettings(SettingsRequest request)
			throws ServerMismatchException;

	/**
	 * 
	 * @param request
	 * @return the global settings associated with this server.
	 */
	GlobalSettingsReply getGlobalSettings(GlobalSettingsRequest request);
}
