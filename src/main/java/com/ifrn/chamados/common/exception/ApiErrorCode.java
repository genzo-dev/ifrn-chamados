package com.ifrn.chamados.common.exception;

public enum ApiErrorCode {
    RESOURCE_NOT_FOUND(404, "Recurso não encontrado"),
    INVALID_REQUEST(400, "Requisição inválida"),
    VALIDATION_ERROR(400, "Erro de validação"),
    DUPLICATE_RESOURCE(409, "Recurso duplicado"),
    RESOURCE_IN_USE(409, "Recurso em uso"),
    INVALID_STATE(409, "Estado inválido"),
    BUSINESS_RULE_VIOLATION(409, "Regra de negócio violada"),
    DATA_INTEGRITY_VIOLATION(409, "Violação de integridade"),
    UNSUPPORTED_MEDIA_TYPE(415, "Tipo de mídia não suportado"),
    METHOD_NOT_ALLOWED(405, "Método não permitido"),
    INTERNAL_ERROR(500, "Erro interno");

    private final int status;
    private final String title;

    ApiErrorCode(int status, String title) {
        this.status = status;
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
