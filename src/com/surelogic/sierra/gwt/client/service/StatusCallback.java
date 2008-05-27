package com.surelogic.sierra.gwt.client.service;

import com.surelogic.sierra.gwt.client.data.Status;

public abstract class StatusCallback extends StandardCallback<Status> {

	@Override
	protected void doSuccess(Status result) {
		if (result == null) {
			result = Status.failure("No result returned");
		}
		doStatus(result);
	}

	protected abstract void doStatus(Status status);

}
