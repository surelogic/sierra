package com.surelogic.sierra.gwt.client.service;

import com.surelogic.sierra.gwt.client.data.Status;

public abstract class StatusCallback extends StandardCallback {

	protected void doSuccess(Object result) {
		Status status = (Status) result;
		if (status == null) {
			status = Status.failure("No result returned");
		}
		doStatus(status);
	}

	protected abstract void doStatus(Status status);

}
