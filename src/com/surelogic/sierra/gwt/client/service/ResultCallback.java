package com.surelogic.sierra.gwt.client.service;

import com.surelogic.sierra.gwt.client.data.Result;

public abstract class ResultCallback extends StandardCallback {

	protected final void doSuccess(Object result) {
		final Result slResult = (Result) result;
		if (slResult == null) {
			doFailure("Null result", null);
		} else if (slResult.isSuccess()) {
			doSuccess(slResult.getMessage(), slResult.getResult());
		} else {
			doFailure(slResult.getMessage(), slResult.getResult());
		}
	}

	protected abstract void doFailure(String message, Object result);

	protected abstract void doSuccess(String message, Object result);

}
