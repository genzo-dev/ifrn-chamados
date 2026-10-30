package com.ifrn.chamados.common.exception;

public class ResourceInUseException extends ApiException {

    public ResourceInUseException(String message) {
        super(ApiErrorCode.RESOURCE_IN_USE, message);
    }
}
