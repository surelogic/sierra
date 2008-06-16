package com.surelogic.sierra.chart.cache;

import java.io.Serializable;
import java.util.UUID;

import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.gwt.client.data.Report;

/**
 * A ticket associates an {@link UUID} with a set of parameters. This class is
 * immutable.
 */
public final class Ticket implements Serializable {

	private final UUID f_id;

	public UUID getUUID() {
		return f_id;
	}

	private final Report f_report;

	public Report getReport() {
		return f_report;
	}

	public Ticket(Report report) {
		if (report == null) {
			throw new IllegalArgumentException(I18N.err(44, "parameters"));
		}
		f_id = UUID.randomUUID();
		f_report = report;
	}

	private static final long serialVersionUID = -6317007809831867466L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((f_id == null) ? 0 : f_id.hashCode());
		result = prime * result
				+ ((f_report == null) ? 0 : f_report.hashCode());
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
		final Ticket other = (Ticket) obj;
		if (f_id == null) {
			if (other.f_id != null) {
				return false;
			}
		} else if (!f_id.equals(other.f_id)) {
			return false;
		}
		if (f_report == null) {
			if (other.f_report != null) {
				return false;
			}
		} else if (!f_report.equals(other.f_report)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "(Ticket UUID=" + f_id.toString() + " report="
				+ f_report.toString() + ")";
	}
}
