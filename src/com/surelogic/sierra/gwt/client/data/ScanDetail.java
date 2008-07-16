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

	private String project;

	private String date;

	private String linesOfCode;

	private String findings;

	private String density;

	private String tools;

	private String packages;
	private String classes;

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

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
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

	public String getProject() {
		return project;
	}

	public void setProject(final String project) {
		this.project = project;
	}

	public String getPackages() {
		return packages;
	}

	public void setPackages(final String packages) {
		this.packages = packages;
	}

	public String getClasses() {
		return classes;
	}

	public void setClasses(final String classes) {
		this.classes = classes;
	}

}
