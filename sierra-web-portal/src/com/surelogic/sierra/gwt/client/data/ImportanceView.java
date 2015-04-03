package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Typesafe Enumeration of importances
 * 
 * @author nathan
 * 
 */
public enum ImportanceView implements Serializable {
	IRRELEVANT("Irrelevant"), LOW("Low"), MEDIUM("Medium"), HIGH("High"), CRITICAL(
			"Critical");

	private final String name;

	private ImportanceView(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static ImportanceView fromString(final String readString) {
		for (final ImportanceView i : values()) {
			if (i.getName().equals(readString)) {
				return i;
			}
		}
		throw new IllegalArgumentException("There is no ImportanceView: "
				+ readString);
	}

	private static ImportanceView[] standardValues = new ImportanceView[] {
			LOW, MEDIUM, HIGH, CRITICAL };

	public static List<ImportanceView> standardValues() {
		final List<ImportanceView> imps = new ArrayList<ImportanceView>(
				standardValues.length);
		for (final ImportanceView i : standardValues) {
			imps.add(i);
		}
		return imps;
	}

}
