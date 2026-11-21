
package com.sanmartin.farmacias.Exception;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Duplicados detectados desde los servicios
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> manejarDuplicado(
            DuplicateResourceException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    // Reglas de negocio e inconsistencias de estado
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> manejarReglasNegocio(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    // Duplicados detectados directamente por Oracle
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarIntegridad(
            DataIntegrityViolationException ex) {

        Throwable causa = ex;

        while (causa != null) {
            if (causa instanceof SQLException sql
                    && sql.getErrorCode() == 1
                    && sql.getMessage() != null
                    && sql.getMessage().toUpperCase(Locale.ROOT)
                    .contains("UK_PRODUCTO_IDENTIDAD")) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message",
                                "Ya existe un producto con el mismo nombre, laboratorio y presentación"
                        ));
            }

            causa = causa.getCause();
        }

        // No ocultar otros errores de integridad
        throw ex;
    }
}
