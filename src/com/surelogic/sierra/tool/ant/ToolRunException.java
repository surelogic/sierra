package com.surelogic.sierra.tool.ant;

import java.util.Map;

import org.apache.tools.ant.BuildException;

public class ToolRunException extends BuildException {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 1L;

	private Map<String, String> f_failedTools;

	public ToolRunException(String message, Map<String, String> failedTools) {
		super(message);
		f_failedTools = failedTools;
	}

	public Map<String, String> getFailedTools() {
		return f_failedTools;
	}

}
