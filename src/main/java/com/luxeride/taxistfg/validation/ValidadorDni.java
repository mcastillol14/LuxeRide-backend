package com.luxeride.taxistfg.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidadorDni implements ConstraintValidator<DniValido, String> {

    // letras de control oficiales del DNI español, el indice es numero % 23
    private static final String LETRAS_CONTROL = "TRWAGMYFPDXBNJZSQVHLCKE";

    @Override
    public boolean isValid(String dni, ConstraintValidatorContext context) {
        if (dni == null || !dni.matches("\\d{8}[A-Z]")) {
            return false;
        }
        int numero = Integer.parseInt(dni.substring(0, 8));
        char letraEsperada = LETRAS_CONTROL.charAt(numero % 23);
        return dni.charAt(8) == letraEsperada;
    }
}
