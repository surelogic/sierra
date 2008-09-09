package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Report implements Serializable, Cacheable {
	public enum DataSource {
		BUGLINK, TEAMSERVER
	}

	public enum OutputType {
		TABLE, CHART, PDF
	}

	private static final long serialVersionUID = -5559871759716632180L;
	private String uuid;
	private String title;
	private long revision;
	private String description;
	private DataSource dataSource;
	private OutputType[] outputTypes;
	private List<Parameter> parameters;
	private List<ReportSettings> savedReports;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(final long revision) {
		this.revision = revision;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public OutputType[] getOutputTypes() {
		if (outputTypes == null) {
			return new OutputType[0];
		}
		return outputTypes;
	}

	public boolean hasOutputType(final OutputType outputType) {
		if (outputTypes == null) {
			return false;
		}
		for (final OutputType supportedType : outputTypes) {
			if (supportedType == outputType) {
				return true;
			}
		}
		return false;
	}

	public void setOutputTypes(final OutputType... outputTypes) {
		this.outputTypes = outputTypes;
	}

	public List<Parameter> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<Parameter>();
		}
		return parameters;
	}

	public Parameter getParameter(final String name) {
		for (final Parameter param : getParameters()) {
			final Parameter found = findParameter(param, name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private Parameter findParameter(final Parameter param, final String name) {
		if (LangUtil.equalsIgnoreCase(param.getName(), name)) {
			return param;
		}
		for (final Parameter child : param.getChildren()) {
			final Parameter found = findParameter(child, name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public List<ReportSettings> getSavedReports() {
		if (savedReports == null) {
			savedReports = new ArrayList<ReportSettings>();
		}
		return savedReports;
	}

	public Report copy() {
		final Report copy = new Report();
		copy.uuid = uuid;
		copy.revision = revision;
		copy.title = title;
		copy.description = description;
		copy.dataSource = dataSource;
		copy.outputTypes = outputTypes;
		for (final Parameter param : getParameters()) {
			copy.getParameters().add(param.copy());
		}
		for (final ReportSettings saved : getSavedReports()) {
			copy.getSavedReports().add(saved.copy());
		}
		return copy;
	}

	@Override
	public String toString() {
		return "{" + title + " " + revision + ": " + title + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + (int) (revision ^ (revision >>> 32));
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());

		result = prime * result
				+ ((dataSource == null) ? 0 : dataSource.ordinal());
		result = prime * result
				+ ((outputTypes == null) ? 0 : Arrays.hashCode(outputTypes));
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Report)) {
			return false;
		}
		final Report other = (Report) obj;
		if (!LangUtil.equals(uuid, other.uuid)) {
			return false;
		}
		if (revision != other.revision) {
			return false;
		}
		if (!LangUtil.equals(description, other.description)) {
			return false;
		}
		if (!LangUtil.equals(title, other.title)) {
			return false;
		}
		if (dataSource != other.dataSource) {
			return false;
		}
		if (outputTypes == null && other.outputTypes != null) {
			return false;
		}
		if (outputTypes != null && other.outputTypes == null) {
			return false;
		}
		if (!Arrays.equals(outputTypes, other.outputTypes)) {
			return false;
		}
		if (!LangUtil.equals(parameters, other.parameters)) {
			return false;
		}
		return true;
	}

	public static class Parameter implements Serializable {
		private static final long serialVersionUID = 3854024638284271950L;

		public enum Type {
			TEXT, IMPORTANCE, PROJECTS, SCANS, CATEGORY, BOOLEAN, FINDING_TYPE
		}

		private String name;
		private String title;
		private Type type;
		private List<Parameter> children;

		public Parameter() {
			super();
		}

		public Parameter(final String name, final String title, final Type type) {
			super();
			this.name = name;
			this.title = title;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(final String title) {
			this.title = title;
		}

		public Type getType() {
			return type;
		}

		public void setType(final Type type) {
			this.type = type;
		}

		public List<Parameter> getChildren() {
			if (children == null) {
				children = new ArrayList<Parameter>();
			}
			return children;
		}

		public void setChildren(final List<Parameter> children) {
			this.children = children;
		}

		public Parameter copy() {
			final Parameter copy = new Parameter(name, title, type);
			final List<Parameter> copyChildren = copy.getChildren();
			for (final Parameter child : getChildren()) {
				copyChildren.add(child.copy());
			}
			return copy;
		}

		@Override
		public String toString() {
			return "{" + title + " (" + name + "): " + type + "}";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((title == null) ? 0 : title.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			if (children != null) {
				for (final Parameter child : children) {
					result = prime * result + child.hashCode();
				}
			}
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Report)) {
				return false;
			}

			final Parameter other = (Parameter) obj;
			if (!LangUtil.equals(name, other.name)) {
				return false;
			}
			if (!LangUtil.equals(title, other.title)) {
				return false;
			}
			if (type != other.type) {
				return false;
			}
			if (!LangUtil.equals(children, other.children)) {
				return false;
			}
			return true;
		}
	}

}
