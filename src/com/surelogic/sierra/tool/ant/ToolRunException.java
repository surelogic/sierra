package com.surelogic.sierra.tool.ant;

import java.util.List;

import org.apache.tools.ant.BuildException;

public class ToolRunException extends BuildException {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 1L;

	private List<String> f_failedTools;

	public ToolRunException(String message, List<String> failedTools) {
		super(message);
		f_failedTools = failedTools;
	}

	public List<String> getFailedTools() {
		return f_failedTools;
	}

}
