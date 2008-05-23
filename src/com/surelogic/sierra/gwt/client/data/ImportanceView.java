package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.surelogic.sierra.gwt.client.util.LangUtil;

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

	public static final ImportanceView IRRELEVANT = new ImportanceView(
			"Irrelevant");
	public static final ImportanceView LOW = new ImportanceView("Low");
	public static final ImportanceView MEDIUM = new ImportanceView("Medium");
	public static final ImportanceView HIGH = new ImportanceView("High");
	public static final ImportanceView CRITICAL = new ImportanceView("Critical");

	private static final ImportanceView[] VALUES = new ImportanceView[] {
			IRRELEVANT, LOW, MEDIUM, HIGH, CRITICAL };

	private final String name;

	private ImportanceView(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof ImportanceView) {
			return LangUtil.equals(name, ((ImportanceView) obj).name);
		}
		return false;
	}

	public static ImportanceView[] values() {
		return VALUES;
	}

	public static ImportanceView fromString(String readString) {
		for (int i = 0; i < VALUES.length; i++) {
			if (VALUES[i].getName().equals(readString)) {
				return VALUES[i];
			}
		}
		throw new IllegalArgumentException("There is no ImportanceView: "
				+ readString);
	}

}
