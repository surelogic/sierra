package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;

public abstract class StandardCallback implements AsyncCallback {

	public final void onFailure(Throwable caught) {
		ExceptionUtil.handle(caught);
	}

	public final void onSuccess(Object result) {
		doSuccess(result);
	}

	protected abstract void doSuccess(Object result);

}
