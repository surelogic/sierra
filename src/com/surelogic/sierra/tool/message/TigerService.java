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
	 * Return the uid of an audit trail with this match, qualified by the set of
	 * qualifiers.
	 * 
	 * @param m
	 * @return
	 */
	String getAuditTrailUID(Match m, Qualifiers q);

	/**
	 * Commit a list of transactions.
	 * 
	 * @param transactions
	 */
	void commitTransactions(Transactions transactions);

	Transactions getTransactions(Qualifiers q);

}
