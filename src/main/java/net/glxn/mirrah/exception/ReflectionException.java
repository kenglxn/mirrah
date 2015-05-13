package net.glxn.mirrah.exception;

import static java.lang.String.format;

public class ReflectionException extends RuntimeException {

    private static final long serialVersionUID = -4309252260252389963L;

    public ReflectionException(String message, Throwable underlyingException) {
        super(message, underlyingException);
    }

    public ReflectionException(Throwable t) {
        super(t);
    }

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String format, Object... args) {
        this(format(format, args));
    }
}
