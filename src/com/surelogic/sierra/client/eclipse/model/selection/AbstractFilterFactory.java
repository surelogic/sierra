package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Set;

public abstract class AbstractFilterFactory implements ISelectionFilterFactory {

	public final int compareTo(ISelectionFilterFactory o) {
		return getFilterLabel().compareTo(o.getFilterLabel());
	}

	@Override
	public final boolean equals(Object o) {
	  if (o instanceof ISelectionFilterFactory) {
	    ISelectionFilterFactory f = (ISelectionFilterFactory) o;
	    return getFilterLabel().equals(f.getFilterLabel());
	  }
	  return false;
	}
	
	@Override
  public final int hashCode() {
	  return getFilterLabel().hashCode();
  }
	
	@Override
	public String toString() {
		return getFilterLabel();
	}
	
	public boolean addWhereClauseIfUnusedFilter(Set<ISelectionFilterFactory> unused,
			                                    StringBuilder b, boolean first,
			                                    boolean usesJoin) {
		return first;
	}
}
