package com.surelogic.sierra.tool.message;


/**
 * This class of exceptions is thrown by client implementations of
 * {@link SierraService}.
 *
 * @author nathan
 *
 */
public class SierraServiceClientException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1505452896619669024L;

    public SierraServiceClientException() {
        super();
    }

    public SierraServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SierraServiceClientException(String message) {
        super(message);
    }

    public SierraServiceClientException(Throwable cause) {
        super(cause);
    }
}
