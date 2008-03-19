package com.surelogic.sierra.chart.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.surelogic.common.i18n.I18N;

/**
 * A ticket associates an {@link UUID} with a set of parameters. This class is
 * immutable.
 */
public final class Ticket implements Serializable {

	private final UUID f_id;

	public UUID getUUID() {
		return f_id;
	}

	private final Map<String, String> f_parameters;

	public Map<String, String> getParameters() {
		return f_parameters;
	}

	public Ticket(Map<String, String> parameters) {
		if (parameters == null)
			throw new IllegalArgumentException(I18N.err(44, "parameters"));
		f_id = UUID.randomUUID();
		f_parameters = Collections.unmodifiableMap(parameters);
	}

	private static final long serialVersionUID = -6317007809831867466L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((f_id == null) ? 0 : f_id.hashCode());
		result = prime * result
				+ ((f_parameters == null) ? 0 : f_parameters.hashCode());
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
		final Ticket other = (Ticket) obj;
		if (f_id == null) {
			if (other.f_id != null)
				return false;
		} else if (!f_id.equals(other.f_id))
			return false;
		if (f_parameters == null) {
			if (other.f_parameters != null)
				return false;
		} else if (!f_parameters.equals(other.f_parameters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(Ticket UUID=" + f_id.toString() + " parameters="
				+ f_parameters.toString() + ")";
	}
}
