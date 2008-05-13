package com.surelogic.sierra.gwt.client;

public interface Lifecycle {

	boolean isInitialized();

	boolean isActive();

	void update(Context context);

	void deactivate();

}
