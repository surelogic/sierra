package com.surelogic.sierra.gwt.client.service;

import java.io.Serializable;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;

public abstract class Callback<T extends Serializable> implements
		AsyncCallback<Result<T>> {
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
		ExceptionUtil.log(caught);
		onException(caught);
	}

	public final void onSuccess(Result<T> result) {
		if (debugMessage != null) {
			showDebugSuccess(result);
		}
		if (result.isSuccess()) {
			onSuccess(result.getMessage(), result.getResult());
		} else {
			onFailure(result.getMessage(), result.getResult());
		}
	}

	protected void onException(Throwable caught) {
		ContextManager
				.logout("Unable to communicate with server. (Server may be down)");
	}

	protected abstract void onSuccess(String message, T result);

	protected abstract void onFailure(String message, T result);

	private void showDebugFailure(Throwable caught) {
		showDebugMessage("onFailure", caught.toString());
	}

	private void showDebugSuccess(Result<T> result) {
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
