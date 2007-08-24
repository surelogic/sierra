package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class Error {

	public static class Builder {
		private String tool;
		private String message;

		Builder() {
			clear();
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder tool(String message) {
			this.message = message;
			return this;
		}

		public Error builder() {
			Error e = new Error(this);
			clear();
			return e;
		}

		private void clear() {
			this.tool = null;
			this.message = null;
		}

	}

	private String tool;
	private String message;

	public Error() {

		// Nothing to do
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((tool == null) ? 0 : tool.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Error other = (Error) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (tool == null) {
			if (other.tool != null)
				return false;
		} else if (!tool.equals(other.tool))
			return false;
		return true;
	}

}
