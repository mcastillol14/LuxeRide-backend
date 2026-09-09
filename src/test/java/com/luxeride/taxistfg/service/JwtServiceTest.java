package com.luxeride.taxistfg.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    // clave de test, base64 de 32+ bytes, no tiene nada que ver con la real
    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString("clave-de-test-solo-para-unit-tests-32b+".getBytes());

    @Mock
    private UserDetails userDetails;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROL_CLIENTE"));
        when(userDetails.getUsername()).thenReturn("cliente@luxeride.com");
        doReturn(authorities).when(userDetails).getAuthorities();
    }

    @Test
    void generaUnTokenValidoParaElUsuario() {
        String token = jwtService.getToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValidToken(token)).isTrue();
    }

    @Test
    void tokenGarbageEsInvalido() {
        assertThat(jwtService.isValidToken("esto-no-es-un-jwt")).isFalse();
    }

    @Test
    void tokenMalformadoNoRompeIsValidToken() {
        assertThat(jwtService.isValidToken("a.b.c")).isFalse();
        assertThat(jwtService.isValidToken("")).isFalse();
    }

    @Test
    void tokenExpiradoEsInvalido() {
        Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET));
        String expiredToken = Jwts.builder()
                .setSubject("cliente@luxeride.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThat(jwtService.isValidToken(expiredToken)).isFalse();
    }

    @Test
    void getUsernameFromTokenDevuelveElMismoUsername() {
        String token = jwtService.getToken(userDetails);

        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("cliente@luxeride.com");
    }

    @Test
    void getRolesFromTokenDevuelveLosMismosRoles() {
        String token = jwtService.getToken(userDetails);

        List<String> roles = jwtService.getRolesFromToken(token);

        assertThat(roles).containsExactly("ROLE_ROL_CLIENTE");
    }
}
