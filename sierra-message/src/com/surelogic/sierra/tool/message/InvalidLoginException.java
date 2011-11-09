package com.surelogic.sierra.tool.message;

public class InvalidLoginException extends SierraServiceClientException {
    /**
     *
     */
    private static final long serialVersionUID = -1522955547554626390L;

    public InvalidLoginException() {
        super();
    }

    public InvalidLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(Throwable cause) {
        super(cause);
    }
}
