package ru.concerteza.util.except;

/**
 * User: alexey
 * Date: 10/18/11
 *
 * Runtime exception, that carries important business-logic error message,
 * that should be finally returned to app client
 *
 */

public abstract class MessageException extends RuntimeException {
    private static final long serialVersionUID = 4668320002581645785L;

    protected MessageException() {
    }

    protected MessageException(String message) {
        super(message);
    }

    protected MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    protected MessageException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        String s = getClass().getSimpleName();
        String message = getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}