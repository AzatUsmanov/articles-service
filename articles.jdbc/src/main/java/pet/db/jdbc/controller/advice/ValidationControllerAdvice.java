package pet.db.jdbc.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class ValidationControllerAdvice {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateUserException(DuplicateUserException e, Locale locale) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "duplicate_field",
                        "field", e.getFieldName()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = extractListOfErrors(e);
        return ResponseEntity
                .unprocessableEntity()
                .body(Map.of(
                        "error", "validation_field",
                        "field_errors", errors.toString()
                ));
    }

    private List<String> extractListOfErrors(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
    }

}
