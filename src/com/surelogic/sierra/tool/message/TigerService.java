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

	public String publishRun(@WebParam(name = "run")
	Run run);

	public Qualifiers getQualifiers();
	
	public String getTrail(Match m);
	
	public void commitTransactions(CommitTransactions transactions);

}
