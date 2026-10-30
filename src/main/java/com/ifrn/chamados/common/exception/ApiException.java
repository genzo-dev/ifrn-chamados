package com.ifrn.chamados.common.exception;

public abstract class ApiException extends RuntimeException {

    private final ApiErrorCode code;

    protected ApiException(ApiErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ApiErrorCode getCode() {
        return code;
    }
}
