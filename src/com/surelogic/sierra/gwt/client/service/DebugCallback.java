package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DebugCallback implements AsyncCallback {
	private final String debugMessage;
	private final AsyncCallback target;

	public DebugCallback(AsyncCallback target) {
		super();
		debugMessage = null;
		this.target = target;
	}

	public DebugCallback(String debugMessage, AsyncCallback target) {
		super();
		this.debugMessage = debugMessage;
		this.target = target;
	}

	public void onFailure(Throwable caught) {
		showDebugMessage("onFailure", caught.toString());

		target.onFailure(caught);
	}

	public void onSuccess(Object result) {
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
		alertMsg.append('(').append(param.toString()).append(')');
		Window.alert(alertMsg.toString());
	}
}
