package com.luxeride.taxistfg.Validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDniTest {

    private final ValidadorDni validador = new ValidadorDni();

    @Test
    void dniConLetraDeControlCorrectaEsValido() {
        // pares numero/letra calculados con el mismo algoritmo mod 23 que usa la clase
        assertThat(validador.isValid("12345678Z", null)).isTrue();
        assertThat(validador.isValid("00000000T", null)).isTrue();
        assertThat(validador.isValid("87654321X", null)).isTrue();
        assertThat(validador.isValid("11111111H", null)).isTrue();
    }

    @Test
    void dniConLetraDeControlIncorrectaEsInvalido() {
        assertThat(validador.isValid("12345678A", null)).isFalse();
        assertThat(validador.isValid("00000000A", null)).isFalse();
    }

    @Test
    void formatoInvalidoEsInvalido() {
        assertThat(validador.isValid("1234567Z", null)).isFalse();
        assertThat(validador.isValid("123456789", null)).isFalse();
        assertThat(validador.isValid("12345678z", null)).isFalse();
        assertThat(validador.isValid("1234567ZZ", null)).isFalse();
        assertThat(validador.isValid("", null)).isFalse();
    }

    @Test
    void nullEsInvalido() {
        assertThat(validador.isValid(null, null)).isFalse();
    }
}
