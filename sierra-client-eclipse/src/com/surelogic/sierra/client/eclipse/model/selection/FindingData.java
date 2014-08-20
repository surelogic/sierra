package com.surelogic.sierra.client.eclipse.model.selection;

import com.surelogic.Immutable;
import com.surelogic.sierra.tool.message.Importance;

/**
 * A helper class to hold a model of all the information about one finding. This
 * class is used to help sort and display information in the list of findings
 * (the 'Show' list). This data populates the columns in this display.
 */
@Immutable
public class FindingData {
  public final long f_findingId; // identity
  public final String f_summary;
  public final Importance f_importance;
  public final String f_projectName;
  public final String f_packageName;
  public final String f_typeName;
  public final int f_lineNumber;
  public final String f_findingType;
  public final String f_findingTypeName;
  public final String f_toolName;

  public FindingData(long findingId, String summary, Importance importance, String projectName, String packageName,
      String typeName, int lineNumber, String findingType, String findingTypeName, String toolName) {
    f_findingId = findingId;

    f_summary = summary;
    f_importance = importance;
    f_projectName = projectName;
    f_packageName = packageName;
    f_typeName = typeName;
    f_lineNumber = lineNumber;
    f_findingType = findingType;
    f_findingTypeName = findingTypeName;
    f_toolName = toolName;
  }

  @Override
  public String toString() {
    return "finding_id=" + f_findingId + " [" + f_importance + "] of type " + f_findingType + " \"" + f_summary + "\" in "
        + f_projectName + " " + f_packageName + "." + f_typeName + " at line " + f_lineNumber + " from " + f_toolName;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof FindingData) {
      final FindingData other = (FindingData) o;
      return f_findingId == other.f_findingId;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (int) f_findingId * 31;
  }
}
