package com.surelogic.sierra.message.srpc;

enum ResponseStatus {
	/**
	 * Represents an okay status. A response will be returned as usual.
	 */
	OK((byte) 'k'),
	/**
	 * There has been a remote failure of some sort on the server. An unchecked
	 * exception must be raised on the client.
	 */
	FAIL((byte) 'f'),
	/**
	 * A checked exception was raised on the server. It will be propagated
	 * through to the client.
	 */
	RAISED((byte) 'r'),
	/**
	 * The client made a request to the server, but it used a different version
	 * of the protocol.
	 */
	VERSION((byte) 'v');

	private final byte f_byte;

	ResponseStatus(final byte b) {
		f_byte = b;
	}

	byte getByte() {
		return f_byte;
	}

	public static ResponseStatus fromByte(final byte b) {
		for (final ResponseStatus s : values()) {
			if (b == s.getByte()) {
				return s;
			}
		}
		throw new IllegalArgumentException("Invalid byte.");
	}
}
