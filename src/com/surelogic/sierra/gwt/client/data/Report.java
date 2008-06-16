package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
		for (final Parameter param : parameters) {
			if (LangUtil.equals(param.getName(), name)) {
				return param;
			}
		}
		return null;
	}

	public Report copy() {
		final Report copy = new Report();
		copy.uuid = uuid;
		copy.revision = revision;
		copy.name = name;
		copy.title = title;
		copy.description = description;
		for (final Parameter param : parameters) {
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

		public Parameter(String name) {
			super();
			this.name = name;
		}

		public Parameter(String name, String value) {
			super();
			this.name = name;
			values.add(value);
		}

		public Parameter(String name, String value, Type type) {
			super();
			this.name = name;
			values.add(value);
			this.type = type;
		}

		public Parameter(String name, Collection<String> values) {
			super();
			this.name = name;
			values.addAll(values);
		}

		public Parameter(String name, Collection<String> values, Type type) {
			super();
			this.name = name;
			values.addAll(values);
			this.type = type;
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

		public String getValue() {
			if (values.isEmpty()) {
				return null;
			}
			return values.get(0);
		}

		public List<String> getValues() {
			return values;
		}

		public Parameter copy() {
			final Parameter copy = new Parameter(name, type);
			copy.getValues().addAll(values);
			return copy;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result
					+ ((values == null) ? 0 : values.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Parameter other = (Parameter) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			} else if (!type.equals(other.type)) {
				return false;
			}
			if (values == null) {
				if (other.values != null) {
					return false;
				}
			} else if (!values.equals(other.values)) {
				return false;
			}
			return true;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + (int) (revision ^ (revision >>> 32));
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Report other = (Report) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (revision != other.revision) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

}
