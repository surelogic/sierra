package com.surelogic.sierra.message.srpc;

enum ResponseStatus {
	/**
	 * Represents an okay status. A response will be returned as usual.
	 */
	OK,
	/**
	 * There has been a remote failure of some sort on the server. An unchecked
	 * exception must be raised on the client.
	 */
	FAIL,
	/**
	 * A checked exception was raised on the server. It will be propagated
	 * through the client,
	 */
	RAISED;
}
