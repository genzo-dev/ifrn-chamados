package com.ifrn.chamados.common.exception;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(ApiErrorCode.INVALID_REQUEST, message);
    }
}
