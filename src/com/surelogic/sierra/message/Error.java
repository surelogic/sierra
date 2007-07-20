package com.surelogic.sierra.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Error {

	public static class Builder {
		private String tool;
		private String message;

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder tool(String message) {
			this.message = message;
			return this;
		}

		public Error builder() {
			return new Error(this);
		}

	}

	private String tool;
	private String message;

	public Error() {
	}

	public Error(Builder builder) {
		this.tool = builder.tool;
		this.message = builder.message;
	}

	public String getTool() {
		return tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
