package com.sprintap.usermanagement.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating consistent API responses.
 * Reduces code duplication across controllers by providing
 * standardized response creation methods.
 */
public final class ResponseHelper {

    private ResponseHelper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Create a success response with message and timestamp.
     *
     * @param message the success message
     * @return response map with message and timestamp
     */
    public static Map<String, Object> success(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    /**
     * Create a success response with message, data, and timestamp.
     *
     * @param message the success message
     * @param dataKey the key for the data field
     * @param data    the data object
     * @return response map with message, data, and timestamp
     */
    public static Map<String, Object> success(String message, String dataKey, Object data) {
        Map<String, Object> response = success(message);
        response.put(dataKey, data);
        return response;
    }

    /**
     * Create a 201 CREATED response with message and data.
     *
     * @param message the success message
     * @param dataKey the key for the data field
     * @param data    the created resource
     * @return ResponseEntity with 201 status
     */
    public static ResponseEntity<Map<String, Object>> created(String message, String dataKey, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success(message, dataKey, data));
    }

    /**
     * Create a 200 OK response with message only.
     *
     * @param message the success message
     * @return ResponseEntity with 200 status
     */
    public static ResponseEntity<Map<String, Object>> ok(String message) {
        return ResponseEntity.ok(success(message));
    }

    /**
     * Create a 200 OK response with message and data.
     *
     * @param message the success message
     * @param dataKey the key for the data field
     * @param data    the data object
     * @return ResponseEntity with 200 status
     */
    public static ResponseEntity<Map<String, Object>> ok(String message, String dataKey, Object data) {
        return ResponseEntity.ok(success(message, dataKey, data));
    }
}

