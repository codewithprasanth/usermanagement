package com.sprintap.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Access Denied");
        response.put("message", "You do not have the required role to access this resource");
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid Token");
        response.put("message", "The JWT token is invalid or expired");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationCredentialsNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Authentication Required");
        response.put("message", "Authentication token is required");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Custom Domain Exceptions
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoleNotFoundException(RoleNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Role Not Found");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(RoleInUseException.class)
    public ResponseEntity<Map<String, Object>> handleRoleInUseException(RoleInUseException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Role In Use");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PrivilegeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePrivilegeNotFoundException(PrivilegeNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Privilege Not Found");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGroupNotFoundException(GroupNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Group Not Found");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "User Not Found");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperationException(InvalidOperationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid Operation");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // DOA Rule Exceptions
    @ExceptionHandler(com.sprintap.doarules.exception.DoaRuleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDoaRuleNotFoundException(
            com.sprintap.doarules.exception.DoaRuleNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "DOA Rule Not Found");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.sprintap.doarules.exception.EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            com.sprintap.doarules.exception.EntityNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Entity Not Found");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Validation Exception Handler
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation Failed");
        response.put("message", "Please check the input fields");
        response.put("fieldErrors", fieldErrors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

