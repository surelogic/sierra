package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.ClientContext;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public abstract class Callback implements AsyncCallback {
	private final String debugMessage;

	public Callback() {
		super();
		debugMessage = null;
	}

	public Callback(String debugMessage) {
		super();
		this.debugMessage = debugMessage;
	}

	public final void onFailure(Throwable caught) {
		if (debugMessage != null) {
			showDebugFailure(caught);
		}
		ExceptionTracker.logException(caught);
		onException(caught);
	}

	public final void onSuccess(Object result) {
		Result slResult = (Result) result;
		if (debugMessage != null) {
			showDebugSuccess(slResult);
		}
		if (slResult.isSuccess()) {
			onSuccess(slResult.getMessage(), slResult.getResult());
		} else {
			onFailure(slResult.getMessage(), slResult.getResult());
		}
	}

	protected void onException(Throwable caught) {
		ClientContext
				.logout("Unable to communicate with server. (Server may be down)");
	}

	protected abstract void onSuccess(String message, Object result);

	protected abstract void onFailure(String message, Object result);

	private void showDebugFailure(Throwable caught) {
		showDebugMessage("onFailure", caught.toString());
	}

	private void showDebugSuccess(Result result) {
		final StringBuffer paramBuf = new StringBuffer();
		paramBuf.append(result.isSuccess()).append(',');
		paramBuf.append(result.getMessage()).append(',');
		if (result.getResult() != null) {
			paramBuf.append(result.getResult().toString());
		} else {
			paramBuf.append("null");
		}
		showDebugMessage("onSuccess", paramBuf.toString());
	}

	private void showDebugMessage(String method, String param) {
		final StringBuffer alertMsg = new StringBuffer(debugMessage);
		alertMsg.append(' ').append(method);
		alertMsg.append('(').append(param.toString()).append(')');
		Window.alert(alertMsg.toString());
	}
}
