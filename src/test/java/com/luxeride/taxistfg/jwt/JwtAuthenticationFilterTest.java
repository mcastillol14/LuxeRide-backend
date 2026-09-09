package com.luxeride.taxistfg.jwt;

import com.luxeride.taxistfg.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tokenValidoSeteaLaAutenticacionYContinuaLaCadena() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer buen.token.aqui");
        when(jwtService.isValidToken("buen.token.aqui")).thenReturn(true);
        when(jwtService.getUsernameFromToken("buen.token.aqui")).thenReturn("user@luxeride.com");
        when(jwtService.getRolesFromToken("buen.token.aqui")).thenReturn(List.of("ROLE_ROL_CLIENTE"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@luxeride.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void sinTokenNoSeteaAutenticacionPeroContinuaLaCadena() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenInvalidoOExpiradoNoSeteaAutenticacionPeroContinuaLaCadena() throws Exception {
        // este es justo el bug que se arreglo: un token expirado/invalido no debe abortar la request
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token.malo");
        when(jwtService.isValidToken("token.malo")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void unaExcepcionInesperadaNuncaEscapaCrudaHaciaLaResponse() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token.raro");
        when(jwtService.isValidToken("token.raro")).thenThrow(new RuntimeException("boom"));
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
}
