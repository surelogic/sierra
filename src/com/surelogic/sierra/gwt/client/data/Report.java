package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.Set;

public class Report implements Serializable, Cacheable {
	private static final long serialVersionUID = -5559871759716632180L;
	private String uuid;
	private String name;
	private long revision;
	private String description;
	private Set<Parameter> parameters;

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

	public void setParameters(Set<Parameter> parameters) {
		this.parameters = parameters;
	}

	public static class Parameter implements Serializable {
		private static final long serialVersionUID = 3854024638284271950L;

		public enum Type {
			TEXT, PRIORITY, PROJECTS
		};

		private String title;
		private Type type;

		public Parameter() {
			super();
		}

		public Parameter(String title, Type type) {
			super();
			this.title = title;
			this.type = type;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

	}

}
