package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2323420123555798331L;

	private String linesOfCode;

	private String findings;

	private String density;

	private String tools;

	private Map<String, List<String>> compilations;

	public String getLinesOfCode() {
		return linesOfCode;
	}

	public void setLinesOfCode(final String linesOfCode) {
		this.linesOfCode = linesOfCode;
	}

	public String getFindings() {
		return findings;
	}

	public void setFindings(final String findings) {
		this.findings = findings;
	}

	public String getTools() {
		return tools;
	}

	public void setTools(final String tools) {
		this.tools = tools;
	}

	public Map<String, List<String>> getCompilations() {
		if (compilations == null) {
			compilations = new HashMap<String, List<String>>();
		}
		return compilations;
	}

	public String getDensity() {
		return density;
	}

	public void setDensity(final String density) {
		this.density = density;
	}

}
