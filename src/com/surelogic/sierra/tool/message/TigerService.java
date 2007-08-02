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
	Run run);

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
	String getAuditTrail(AuditTrailFind seed);

	/**
	 * Commit a trail of transactions.
	 * 
	 * @param audits
	 * @return the server revision containing the new audits
	 */
	Long commitAuditTrail(AuditTrail audits);

	/**
	 * Get all of the auditing events for a set of qualifiers, after the
	 * specified revision.
	 * 
	 * @param request
	 * @return all audit trails and the audits they contain after the specified
	 *         revision
	 */
	AuditTrails getAuditTrails(AuditTrailRequest request);

}
