package com.luxeride.taxistfg.Service;

import com.luxeride.taxistfg.Model.Rol;
import com.luxeride.taxistfg.Model.Usuario;
import com.luxeride.taxistfg.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder encriptadoPassword;
    @Mock
    private MailService mailService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioService = new UsuarioService(usuarioRepository, encriptadoPassword);
        // mailService solo se autowirea por campo en Spring, aqui lo inyectamos a mano
        ReflectionTestUtils.setField(usuarioService, "mailService", mailService);
    }

    private Usuario usuarioCompleto() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellidos("Perez");
        usuario.setDni("12345678Z");
        usuario.setEmail("juan@luxeride.com");
        usuario.setPassword("secreto");
        return usuario;
    }

    @Test
    void registrarUsuarioOkGuardaConPasswordEncriptadaYRolCliente() throws Exception {
        Usuario usuario = usuarioCompleto();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni(usuario.getDni())).thenReturn(Optional.empty());
        when(encriptadoPassword.encode("secreto")).thenReturn("hash");

        usuarioService.registrarUsario(usuario);

        assertThat(usuario.getPassword()).isEqualTo("hash");
        assertThat(usuario.getRol()).isEqualTo(Rol.ROL_CLIENTE);
        assertThat(usuario.isAccountNonLocked()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(mailService).enviarMail(usuario.getEmail(), usuario.getNombre());
    }

    @Test
    void registrarUsuarioFalloDeMailNoRompeElRegistro() throws Exception {
        Usuario usuario = usuarioCompleto();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni(usuario.getDni())).thenReturn(Optional.empty());
        when(encriptadoPassword.encode(any())).thenReturn("hash");
        doThrow(new jakarta.mail.MessagingException("smtp caido")).when(mailService).enviarMail(any(), any());

        usuarioService.registrarUsario(usuario);

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void registrarUsuarioConEmailYDniExistentesLanzaExcepcion() {
        Usuario usuario = usuarioCompleto();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(new Usuario()));
        when(usuarioRepository.findByDni(usuario.getDni())).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.registrarUsario(usuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DNI y el correo");
    }

    @Test
    void registrarUsuarioConSoloEmailExistenteLanzaExcepcion() {
        Usuario usuario = usuarioCompleto();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(new Usuario()));
        when(usuarioRepository.findByDni(usuario.getDni())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.registrarUsario(usuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo");
    }

    @Test
    void registrarUsuarioConSoloDniExistenteLanzaExcepcion() {
        Usuario usuario = usuarioCompleto();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni(usuario.getDni())).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.registrarUsario(usuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DNI ya existe");
    }

    @Test
    void registrarUsuarioSinNombreLanzaExcepcion() {
        Usuario usuario = usuarioCompleto();
        usuario.setNombre(null);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni(usuario.getDni())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.registrarUsario(usuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void registrarUsuarioSinPasswordLanzaExcepcion() {
        Usuario usuario = usuarioCompleto();
        usuario.setPassword("");
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni(usuario.getDni())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.registrarUsario(usuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contraseña");
    }

    @Test
    void editarUsuarioActualizaSoloLosCamposNoVacios() {
        Usuario existente = usuarioCompleto();
        existente.setId(1);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));

        usuarioService.editarUsuario(1, "NuevoNombre", null, "", null);

        assertThat(existente.getNombre()).isEqualTo("NuevoNombre");
        assertThat(existente.getApellidos()).isEqualTo("Perez");
        assertThat(existente.getDni()).isEqualTo("12345678Z");
        verify(usuarioRepository).save(existente);
    }

    @Test
    void editarUsuarioInexistenteLanzaNotFound() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.editarUsuario(1, "x", null, null, null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void editarUsuarioConEmailYaUsadoPorOtroLanzaBadRequest() {
        Usuario existente = usuarioCompleto();
        existente.setId(1);
        Usuario otro = usuarioCompleto();
        otro.setId(2);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByEmail("otro@luxeride.com")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> usuarioService.editarUsuario(1, null, null, null, "otro@luxeride.com"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void bloquearCuentaOk() {
        Usuario usuario = usuarioCompleto();
        usuario.setAccountNonLocked(true);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        usuarioService.bloquearCuenta(1);

        assertThat(usuario.isAccountNonLocked()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void bloquearCuentaInexistenteLanzaNotFound() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.bloquearCuenta(1))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void desbloquearCuentaOk() {
        Usuario usuario = usuarioCompleto();
        usuario.setAccountNonLocked(false);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        usuarioService.desbloquearCuenta(1);

        assertThat(usuario.isAccountNonLocked()).isTrue();
    }

    @Test
    void addTaxistaOk() {
        Usuario usuario = usuarioCompleto();
        usuario.setRol(Rol.ROL_CLIENTE);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        usuarioService.addTaxista(1);

        assertThat(usuario.getRol()).isEqualTo(Rol.ROL_TAXISTA);
    }

    @Test
    void deleteTaxistaOkCuandoEraTaxista() {
        Usuario usuario = usuarioCompleto();
        usuario.setRol(Rol.ROL_TAXISTA);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        usuarioService.deleteTaxista(1);

        assertThat(usuario.getRol()).isEqualTo(Rol.ROL_CLIENTE);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deleteTaxistaCuandoNoEraTaxistaLanzaBadRequest() {
        Usuario usuario = usuarioCompleto();
        usuario.setRol(Rol.ROL_CLIENTE);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.deleteTaxista(1))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void obtenerTaxistasDelegaEnElRepositorio() {
        List<Usuario> taxistas = List.of(usuarioCompleto());
        when(usuarioRepository.findByRol(Rol.ROL_TAXISTA)).thenReturn(taxistas);

        assertThat(usuarioService.obtenerTaxistas()).isEqualTo(taxistas);
    }

    @Test
    void obtenerUsuariosDTOPorFiltroSinDniUsaFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(List.of(usuarioCompleto()));
        when(usuarioRepository.findAll(pageable)).thenReturn(page);

        Page<com.luxeride.taxistfg.Model.UsuarioDTO> resultado = usuarioService.obtenerUsuariosDTOPorFiltro(pageable, null);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getEmail()).isEqualTo("juan@luxeride.com");
    }

    @Test
    void obtenerUsuariosDTOPorFiltroConDniUsaFindByDni() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(List.of(usuarioCompleto()));
        when(usuarioRepository.findByDni(pageable, "1234")).thenReturn(page);

        Page<com.luxeride.taxistfg.Model.UsuarioDTO> resultado = usuarioService.obtenerUsuariosDTOPorFiltro(pageable, "1234");

        assertThat(resultado.getContent()).hasSize(1);
        verify(usuarioRepository, never()).findAll(pageable);
    }
}
