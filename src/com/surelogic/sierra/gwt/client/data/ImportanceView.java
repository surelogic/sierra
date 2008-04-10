package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;

/**
 * Typesafe Enumeration of importances
 * 
 * @author nathan
 * 
 */
public final class ImportanceView implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2718025202305850540L;

	public static ImportanceView IRRELEVANT = new ImportanceView("Irrelevant");
	public static ImportanceView LOW = new ImportanceView("Low");
	public static ImportanceView MEDIUM = new ImportanceView("Medium");
	public static ImportanceView HIGH = new ImportanceView("High");
	public static ImportanceView CRITICAL = new ImportanceView("Critical");

	private static final ImportanceView[] VALUES = new ImportanceView[] {
			IRRELEVANT, LOW, MEDIUM, HIGH, CRITICAL };

	private final String name;

	private ImportanceView(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!GWT.getTypeName(obj).equals(GWT.getTypeName(this))) {
			return false;
		}
		final ImportanceView other = (ImportanceView) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	static ImportanceView fromString(String readString) {
		for (int i = 0; i < VALUES.length; i++) {
			if (VALUES[i].getName().equals(readString)) {
				return VALUES[i];
			}
		}
		throw new IllegalArgumentException("There is no ImportanceView: "
				+ readString);
	}

}
