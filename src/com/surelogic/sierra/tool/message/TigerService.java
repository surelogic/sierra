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
	 * @return
	 */
	String getAuditTrail(AuditTrailSeed seed);

	/**
	 * Commit a trail of transactions.
	 * 
	 * @param transactions
	 */
	void commitAuditTrail(AuditTrail transactions);

	/**
	 * Get all of the auditing events for a set of qualifiers.
	 * 
	 * @param q
	 * @return
	 */
	AuditTrails getAuditTrails(Qualifiers q);

}
