package com.techrepair.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {
        int statusCode = ex.getStatusCode().value();
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        String erro = httpStatus != null ? httpStatus.getReasonPhrase() : "Erro na requisição";

        ApiErrorResponseDTO body = new ApiErrorResponseDTO(
                Instant.now(),
                statusCode,
                erro,
                ex.getReason(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(statusCode).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ApiErrorResponseDTO.CampoErroDTO> campos = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ApiErrorResponseDTO.CampoErroDTO(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ApiErrorResponseDTO body = new ApiErrorResponseDTO(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação nos dados enviados",
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponseDTO> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request) {
        ApiErrorResponseDTO body = new ApiErrorResponseDTO(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Credenciais inválidas ou não autorizadas",
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        ApiErrorResponseDTO body = new ApiErrorResponseDTO(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocorreu um erro interno inesperado no servidor",
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
