package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Java class for Importance.
 */
@XmlEnum
public enum Importance {
  IRRELEVANT, LOW, MEDIUM, HIGH, CRITICAL;

  private final String value = toString().substring(0, 1) + toString().toLowerCase().substring(1);

  public static Importance fromValue(final String v) {
    for (final Importance i : values()) {
      if (i.value.equals(v)) {
        return i;
      }
    }

    return valueOf(v.toUpperCase());
  }

  public String toStringSentenceCase() {
    return value;
  }

  private static Importance[] standardValues = new Importance[] { LOW, MEDIUM, HIGH, CRITICAL };

  public static List<Importance> standardValues() {
    final List<Importance> imps = new ArrayList<>(standardValues.length);
    for (final Importance i : standardValues) {
      imps.add(i);
    }
    return imps;
  }

}
