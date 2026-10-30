package com.ifrn.chamados.common.exception;

public class InvalidStateException extends ApiException {

    public InvalidStateException(String message) {
        super(ApiErrorCode.INVALID_STATE, message);
    }
}
