package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.surelogic.sierra.tool.config.Config;

@XmlRootElement
@XmlType
public class Run {

	public static class Builder {
		private Config config;
		private ToolOutput toolOutput;

		public Builder toolOutput(ToolOutput toolOutput) {
			this.toolOutput = toolOutput;
			return this;
		}

		public Builder config(Config config) {
			this.config = config;
			return this;
		}

		public Run build() {
			return new Run(this);
		}

	}

	private Config config;
	private ToolOutput toolOutput;

	public Run() {
		// Nothing to do
	}

	public Run(Builder builder) {
		this.toolOutput = builder.toolOutput;
		this.config = builder.config;
	}

	public ToolOutput getToolOutput() {
		return toolOutput;
	}

	public void setToolOutput(ToolOutput toolOutput) {
		this.toolOutput = toolOutput;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((toolOutput == null) ? 0 : toolOutput.hashCode());
		result = prime * result + ((config == null) ? 0 : config.hashCode());
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
		final Run other = (Run) obj;
		if (toolOutput == null) {
			if (other.toolOutput != null)
				return false;
		} else if (!toolOutput.equals(other.toolOutput))
			return false;
		if (config == null) {
			if (other.config != null)
				return false;
		}
		return true;
	}

}
