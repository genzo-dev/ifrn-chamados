package com.ifrn.chamados.common.exception;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String entity, Long id) {
        super(ApiErrorCode.RESOURCE_NOT_FOUND, entity + " com ID " + id + " não encontrado");
    }

    public ResourceNotFoundException(String message) {
        super(ApiErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
