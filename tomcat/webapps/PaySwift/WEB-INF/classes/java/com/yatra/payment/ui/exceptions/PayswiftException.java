package com.yatra.payment.ui.exceptions;

public class PayswiftException extends RuntimeException {
    private String errorCode;
    private String message;

    public PayswiftException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }

    public PayswiftException(String errorCode, String message) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }

    public PayswiftException(Throwable cause) {
        super(cause);
        this.message = cause.getMessage();
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return this.errorCode + ":" + this.message;
    }

}
