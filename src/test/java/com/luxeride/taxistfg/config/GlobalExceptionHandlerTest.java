package com.luxeride.taxistfg.config;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void validationExceptionDevuelve400ConUnErrorPorCampo() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "usuario");
        bindingResult.addError(new FieldError("usuario", "dni", "Por favor, proporcione un DNI válido"));
        bindingResult.addError(new FieldError("usuario", "email", "Por favor, proporcione una dirección de correo electrónico válida"));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("dni", "Por favor, proporcione un DNI válido")
                .containsEntry("email", "Por favor, proporcione una dirección de correo electrónico válida");
    }

    @Test
    void constraintViolationDevuelve400ConElMensaje() {
        ConstraintViolationException ex = new ConstraintViolationException("dni invalido", Set.of());

        ResponseEntity<String> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("dni invalido");
    }
}
