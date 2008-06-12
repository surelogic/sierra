package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum AssuranceType {
	CONSISTENT, INCONSISTENT;

	private final String value = toString().substring(0, 1)
			+ toString().toLowerCase().substring(1);

	private final String flag = toString().substring(0, 1);

	public static AssuranceType fromValue(String v) {
		for (final AssuranceType i : values()) {
			if (i.value.equals(v)) {
				return i;
			}
		}

		return valueOf(v.toUpperCase());
	}

	public String flag() {
		return flag;
	}

	public String toStringSentenceCase() {
		return value;
	}
}
