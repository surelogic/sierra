package com.surelogic.sierra.message.srpc;

class Response {

	private final ResponseStatus status;
	private final Object response;

	public Response(ResponseStatus status, Object response) {
		this.status = status;
		this.response = response;
	}

	public ResponseStatus getStatus() {
		return status;
	}

	public Object getResponse() {
		return response;
	}

}
