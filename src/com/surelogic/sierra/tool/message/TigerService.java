package com.surelogic.sierra.tool.message;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

@WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.BARE)
public interface TigerService {

	/**
	 * Publish a run to the server.
	 * 
	 * @param run
	 * @return whether or not the run was successfully generated on the server.
	 */
	String publishRun(@WebParam(name = "run")
	Scan run);

	/**
	 * Get the set of available qualifier names.
	 * 
	 * @return
	 */
	Qualifiers getQualifiers();

	/**
	 * Return the uid of an audit trail that matches the given seed. If none
	 * matches, a new audit trail will be created.
	 * 
	 * @param seed
	 * @return the unique identifier of a global audit trail.
	 */
	MergeAuditResponse mergeAuditTrails(MergeAuditTrailRequest seed);

	/**
	 * Commit an audit trail of transactions. The audit trail consists of any
	 * new match locations that have been added to the trail, as well as any new
	 * auditing events.
	 * 
	 * @param audits
	 * @return the server revision containing the new audits
	 */
	CommitAuditResponse commitAuditTrails(AuditTrails audits);

	/**
	 * Get all of the auditing events for the given qualifier and project
	 * occurring after the specified revision.
	 * 
	 * @param request
	 * @return all audit trails and the audits they contain after the specified
	 *         revision
	 */
	AuditTrailResponse getAuditTrails(AuditTrailRequest request);

	/**
	 * Return the settings associated with a given qualifier.
	 * 
	 * @param request
	 * @return the settings associated with a given qualifier.
	 */
	SettingsReply getSettings(SettingsRequest request);

}
