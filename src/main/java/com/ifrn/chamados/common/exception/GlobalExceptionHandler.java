package com.ifrn.chamados.common.exception;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(ApiException exception, HttpServletRequest request) {
        return respond(exception.getCode(), exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, List<String>> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.computeIfAbsent(error.getField(), field -> new ArrayList<>())
                        .add(error.getDefaultMessage()));
        exception.getBindingResult().getGlobalErrors().forEach(error ->
                errors.computeIfAbsent("_global", field -> new ArrayList<>())
                        .add(error.getDefaultMessage()));

        ProblemDetail problem = problem(
                ApiErrorCode.VALIDATION_ERROR,
                "Dados inválidos ou fornecidos incorretamente.",
                request
        );
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        Map<String, List<String>> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.computeIfAbsent(violation.getPropertyPath().toString(), field -> new ArrayList<>())
                        .add(violation.getMessage()));

        ProblemDetail problem = problem(
                ApiErrorCode.VALIDATION_ERROR,
                "Dados inválidos ou fornecidos incorretamente.",
                request
        );
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return respond(ApiErrorCode.INVALID_REQUEST, "Corpo da requisição inválido ou ausente.", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        return respond(ApiErrorCode.INVALID_REQUEST, "Parâmetro inválido: " + exception.getName(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {

        return respond(
                ApiErrorCode.INVALID_REQUEST,
                "Parâmetro obrigatório ausente: " + exception.getParameterName(),
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {

        return respond(ApiErrorCode.RESOURCE_NOT_FOUND, "Recurso não encontrado.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        return respond(
                ApiErrorCode.METHOD_NOT_ALLOWED,
                "Método HTTP não suportado para este recurso.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {

        return respond(
                ApiErrorCode.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de mídia não suportado. Use application/json.",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        log.warn("Violação de integridade dos dados", exception);
        return respond(
                ApiErrorCode.DATA_INTEGRITY_VIOLATION,
                "Violação de integridade dos dados.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        log.error("Erro inesperado ao processar a requisição", exception);
        return respond(ApiErrorCode.INTERNAL_ERROR, "Erro interno inesperado.", request);
    }

    private ResponseEntity<ProblemDetail> respond(
            ApiErrorCode code,
            String detail,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatusCode.valueOf(code.getStatus()))
                .body(problem(code, detail, request));
    }

    private ProblemDetail problem(ApiErrorCode code, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(code.getStatus()),
                detail
        );
        problem.setTitle(code.getTitle());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code.name());
        return problem;
    }
}
