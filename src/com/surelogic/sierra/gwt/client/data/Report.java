package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Report implements Serializable, Cacheable {
	private static final long serialVersionUID = -5559871759716632180L;
	private String uuid;
	private String name;
	private String title;
	private long revision;
	private String description;
	private final Set<Parameter> parameters = new HashSet<Parameter>();

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Parameter> getParameters() {
		return parameters;
	}

	public Parameter getParameter(String name) {
		for (Parameter param : parameters) {
			if (LangUtil.equals(param.getName(), name)) {
				return param;
			}
		}
		return null;
	}

	public Report copy() {
		Report copy = new Report();
		copy.uuid = uuid;
		copy.revision = revision;
		copy.name = name;
		copy.title = title;
		copy.description = description;
		for (Parameter param : parameters) {
			copy.getParameters().add(param.copy());
		}

		return copy;
	}

	public static class Parameter implements Serializable {
		private static final long serialVersionUID = 3854024638284271950L;

		public enum Type {
			TEXT, PRIORITY, PROJECTS
		};

		private String name;
		private Type type;
		private final List<String> values = new ArrayList<String>();

		public Parameter() {
			super();
		}

		public Parameter(String name, Type type) {
			super();
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public List<String> getValues() {
			return values;
		}

		public Parameter copy() {
			Parameter copy = new Parameter(name, type);
			copy.getValues().addAll(values);
			return copy;
		}
	}

}
