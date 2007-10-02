package com.surelogic.sierra.client.eclipse.model.selection;

public interface IFilterObserver {

	void porous(Filter filter);

	void contentsChanged(Filter filter);

	void queryFailure(Filter filter, Exception e);
}
