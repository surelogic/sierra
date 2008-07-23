package com.surelogic.sierra.gwt.client.service.callback;

import com.surelogic.sierra.gwt.client.data.Result;

public abstract class ResultCallback<T> extends StandardCallback<Result<T>> {

	@Override
	protected final void doSuccess(Result<T> result) {
		if (result == null) {
			doFailure("Null result", null);
		} else if (result.isSuccess()) {
			doSuccess(result.getMessage(), result.getResult());
		} else {
			doFailure(result.getMessage(), result.getResult());
		}
	}

	protected abstract void doFailure(String message, T result);

	protected abstract void doSuccess(String message, T result);

}
