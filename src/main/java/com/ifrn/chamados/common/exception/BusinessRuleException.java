package com.ifrn.chamados.common.exception;

public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(ApiErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
