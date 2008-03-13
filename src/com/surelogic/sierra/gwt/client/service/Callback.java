package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.ClientContext;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public abstract class Callback implements AsyncCallback {

	public final void onFailure(Throwable caught) {
		ExceptionTracker.logException(caught);
		onException(caught);
	}

	public final void onSuccess(Object result) {
		Result slResult = (Result) result;
		if (slResult.isSuccess()) {
			onSuccess(slResult.getMessage(), slResult.getResult());
		} else {
			onFailure(slResult.getMessage(), slResult.getResult());
		}
	}

	protected void onException(Throwable caught) {
		ClientContext
				.invalidate("Unable to communicate with server. (Server may be down)");
	}

	protected abstract void onSuccess(String message, Object result);

	protected abstract void onFailure(String message, Object result);

}
