package com.ifrn.chamados.common.exception;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(ApiErrorCode.DUPLICATE_RESOURCE, message);
    }
}
