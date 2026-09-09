package com.luxeride.taxistfg.service;

import com.luxeride.taxistfg.jwt.AuthResponse;
import com.luxeride.taxistfg.entity.Usuario;
import com.luxeride.taxistfg.repository.UsuarioRepository;
import com.luxeride.taxistfg.util.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(usuarioRepository, jwtService, passwordEncoder, authenticationManager);
    }

    @Test
    void loginConCredencialesValidasDevuelveElTokenGenerado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@luxeride.com");
        usuario.setAccountNonLocked(true);
        when(usuarioRepository.findByEmail("cliente@luxeride.com")).thenReturn(Optional.of(usuario));
        when(jwtService.getToken(usuario)).thenReturn("token123");

        LoginRequest request = LoginRequest.builder().email("cliente@luxeride.com").password("secreto").build();

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token123");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void loginConUsuarioInexistenteLanzaUsernameNotFoundException() {
        when(usuarioRepository.findByEmail("noexiste@luxeride.com")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder().email("noexiste@luxeride.com").password("x").build();

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UsernameNotFoundException.class);
        verifyNoInteractions(authenticationManager, jwtService);
    }

    @Test
    void loginConCuentaBloqueadaLanzaRuntimeExceptionYNoLlegaAAutenticar() {
        Usuario usuario = new Usuario();
        usuario.setEmail("bloqueado@luxeride.com");
        usuario.setAccountNonLocked(false);
        when(usuarioRepository.findByEmail("bloqueado@luxeride.com")).thenReturn(Optional.of(usuario));

        LoginRequest request = LoginRequest.builder().email("bloqueado@luxeride.com").password("x").build();

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bloqueada");

        verifyNoInteractions(authenticationManager, jwtService);
    }
}
