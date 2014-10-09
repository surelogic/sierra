package com.surelogic.sierra.client.eclipse.model;

import java.util.Date;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;
import com.surelogic.common.i18n.I18N;

@Immutable
@ValueObject
public final class ScannedProject {

  @NonNull
  private final String f_name;
  @NonNull
  private final Date f_whenScanned;
  @Nullable
  private final Date f_whenScannedPreviously;
  @Nullable
  private final String f_exclusionFilter;

  public ScannedProject(@NonNull String name, @NonNull Date whenScanned, @Nullable Date whenScannedPreviously,
      @Nullable String exclusionFilter) {
    if (name == null)
      throw new IllegalArgumentException(I18N.err(44, "name"));
    if (whenScanned == null)
      throw new IllegalArgumentException(I18N.err(44, "whenScanned"));
    f_name = name;
    f_whenScanned = whenScanned;
    f_whenScannedPreviously = whenScannedPreviously;
    f_exclusionFilter = exclusionFilter;
  }

  @NonNull
  public String getName() {
    return f_name;
  }

  @NonNull
  public Date getWhenScanned() {
    return f_whenScanned;
  }

  @Nullable
  public Date getWhenScannedPreviouslyOrNull() {
    return f_whenScannedPreviously;
  }

  @Nullable
  public String getExclusionFilterOrNull() {
    return f_exclusionFilter;
  }

  @Nullable
  public String getExclusionFilterOrEmptyString() {
    return f_exclusionFilter == null ? "" : f_exclusionFilter;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((f_exclusionFilter == null) ? 0 : f_exclusionFilter.hashCode());
    result = prime * result + ((f_name == null) ? 0 : f_name.hashCode());
    result = prime * result + ((f_whenScanned == null) ? 0 : f_whenScanned.hashCode());
    result = prime * result + ((f_whenScannedPreviously == null) ? 0 : f_whenScannedPreviously.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof ScannedProject))
      return false;
    ScannedProject other = (ScannedProject) obj;
    if (f_exclusionFilter == null) {
      if (other.f_exclusionFilter != null)
        return false;
    } else if (!f_exclusionFilter.equals(other.f_exclusionFilter))
      return false;
    if (f_name == null) {
      if (other.f_name != null)
        return false;
    } else if (!f_name.equals(other.f_name))
      return false;
    if (f_whenScanned == null) {
      if (other.f_whenScanned != null)
        return false;
    } else if (!f_whenScanned.equals(other.f_whenScanned))
      return false;
    if (f_whenScannedPreviously == null) {
      if (other.f_whenScannedPreviously != null)
        return false;
    } else if (!f_whenScannedPreviously.equals(other.f_whenScannedPreviously))
      return false;
    return true;
  }
}
