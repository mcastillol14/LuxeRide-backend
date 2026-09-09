package com.luxeride.taxistfg.Controller;

import com.luxeride.taxistfg.Model.Usuario;
import com.luxeride.taxistfg.Model.Viaje;
import com.luxeride.taxistfg.Repository.UsuarioRepository;
import com.luxeride.taxistfg.Repository.ViajeRepository;
import com.luxeride.taxistfg.Service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ControladorUsuarioTest {

    @Mock private UsuarioService usuarioService;
    @Mock private AuthService authService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CocheService cocheService;
    @Mock private ServicioService servicioService;
    @Mock private ViajeService viajeService;
    @Mock private ViajeRepository viajeRepository;
    @Mock private PdfService pdfService;

    private ControladorUsuario controlador;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controlador = new ControladorUsuario(usuarioService, authService, usuarioRepository,
                cocheService, servicioService, viajeService);
        // viajeRepository y pdfService no son final, @RequiredArgsConstructor no los incluye: se inyectan a mano
        ReflectionTestUtils.setField(controlador, "viajeRepository", viajeRepository);
        ReflectionTestUtils.setField(controlador, "pdfService", pdfService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String email, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Viaje viajeDeEjemplo() {
        Viaje viaje = new Viaje();
        viaje.setHoraLlegada(LocalDateTime.now());
        viaje.setPrecioTotal(BigDecimal.TEN);
        return viaje;
    }

    @Test
    void generarPdfElDuenioPuedeDescargarSuPropioViaje() throws Exception {
        autenticarComo("cliente@luxeride.com", "ROLE_ROL_CLIENTE");
        Usuario usuario = new Usuario();
        usuario.setId(5);
        when(usuarioRepository.findByEmail("cliente@luxeride.com")).thenReturn(Optional.of(usuario));
        when(viajeRepository.findFirstByClienteIdOrderByIdDesc(5)).thenReturn(Optional.of(viajeDeEjemplo()));
        when(pdfService.generarPdfViaje(any())).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<byte[]> response = controlador.generarPdfUltimoViaje(5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void generarPdfOtroUsuarioNoPuedeDescargarElViajeDeOtro() {
        autenticarComo("otro@luxeride.com", "ROLE_ROL_CLIENTE");
        Usuario usuario = new Usuario();
        usuario.setId(99);
        when(usuarioRepository.findByEmail("otro@luxeride.com")).thenReturn(Optional.of(usuario));

        ResponseEntity<byte[]> response = controlador.generarPdfUltimoViaje(5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(viajeRepository, never()).findFirstByClienteIdOrderByIdDesc(any());
    }

    @Test
    void generarPdfElAdminPuedeDescargarElViajeDeCualquiera() throws Exception {
        autenticarComo("admin@luxeride.com", "ROLE_ROL_ADMIN");
        when(viajeRepository.findFirstByClienteIdOrderByIdDesc(5)).thenReturn(Optional.of(viajeDeEjemplo()));
        when(pdfService.generarPdfViaje(any())).thenReturn(new byte[]{9});

        ResponseEntity<byte[]> response = controlador.generarPdfUltimoViaje(5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(usuarioRepository, never()).findByEmail(any());
    }

    @Test
    void generarPdfSinViajesDevuelveNotFound() {
        autenticarComo("admin@luxeride.com", "ROLE_ROL_ADMIN");
        when(viajeRepository.findFirstByClienteIdOrderByIdDesc(5)).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = controlador.generarPdfUltimoViaje(5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void registrarUsuarioOkDevuelve200() {
        Usuario usuario = new Usuario();

        ResponseEntity<String> response = controlador.registrarUsario(usuario);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void registrarUsuarioConErrorDevuelveBadRequest() {
        Usuario usuario = new Usuario();
        doThrow(new IllegalArgumentException("El DNI ya existe")).when(usuarioService).registrarUsario(usuario);

        ResponseEntity<String> response = controlador.registrarUsario(usuario);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("El DNI ya existe");
    }

    @Test
    void obtenerInformacionDevuelveElUsuarioAutenticado() {
        autenticarComo("cliente@luxeride.com", "ROLE_ROL_CLIENTE");
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@luxeride.com");
        usuario.setNombre("Juan");
        when(usuarioRepository.findByEmail("cliente@luxeride.com")).thenReturn(Optional.of(usuario));

        ResponseEntity<?> response = controlador.obtenerInformacion();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void obtenerInformacionUsuarioNoEncontradoDevuelve404() {
        autenticarComo("fantasma@luxeride.com", "ROLE_ROL_CLIENTE");
        when(usuarioRepository.findByEmail("fantasma@luxeride.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controlador.obtenerInformacion();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
