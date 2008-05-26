package com.surelogic.sierra.gwt.client.service;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;

public abstract class StandardCallback<T extends Serializable> implements
		AsyncCallback<T> {

	public final void onFailure(Throwable caught) {
		ExceptionUtil.handle(caught);
	}

	public final void onSuccess(T result) {
		doSuccess(result);
	}

	protected abstract void doSuccess(T result);

}
