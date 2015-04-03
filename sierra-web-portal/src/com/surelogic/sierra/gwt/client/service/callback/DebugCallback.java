package com.surelogic.sierra.gwt.client.service.callback;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DebugCallback<T> implements AsyncCallback<T> {
	private final String debugMessage;
	private final AsyncCallback<T> target;

	public DebugCallback(AsyncCallback<T> target) {
		super();
		debugMessage = null;
		this.target = target;
	}

	public DebugCallback(String debugMessage, AsyncCallback<T> target) {
		super();
		this.debugMessage = debugMessage;
		this.target = target;
	}

	public void onFailure(Throwable caught) {
		showDebugMessage("onFailure", caught.toString());

		target.onFailure(caught);
	}

	public void onSuccess(T result) {
		final StringBuffer paramBuf = new StringBuffer();
		if (result != null) {
			paramBuf.append(result.toString());
		} else {
			paramBuf.append("null");
		}
		showDebugMessage("onSuccess", paramBuf.toString());

		target.onSuccess(result);
	}

	private void showDebugMessage(String method, String param) {
		final StringBuffer alertMsg = new StringBuffer(debugMessage);
		alertMsg.append(' ').append(method);
		alertMsg.append('(').append(param).append(')');
		Window.alert(alertMsg.toString());
	}
}
