package com.luxeride.taxistfg.controller;

import com.luxeride.taxistfg.entity.*;
import com.luxeride.taxistfg.dto.*;
import com.luxeride.taxistfg.service.CocheService;
import com.luxeride.taxistfg.service.LicenciaService;
import com.luxeride.taxistfg.service.ServicioService;
import com.luxeride.taxistfg.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ControladorAdminTest {

    @Mock private CocheService cocheService;
    @Mock private UsuarioService usuarioService;
    @Mock private LicenciaService licenciaService;
    @Mock private ServicioService servicioService;

    private ControladorAdmin controlador;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controlador = new ControladorAdmin(cocheService, usuarioService, licenciaService, servicioService);
    }

    @Test
    void obtenerUsuariosDelegaEnElServicio() {
        Page<UsuarioDTO> page = new PageImpl<>(List.of(new UsuarioDTO()));
        when(usuarioService.obtenerUsuariosDTOPorFiltro(any(), eq("123"))).thenReturn(page);

        assertThat(controlador.obtenerUsuarios(org.springframework.data.domain.Pageable.unpaged(), "123")).isEqualTo(page);
    }

    @Test
    void editarUsuarioOk() {
        ResponseEntity<String> response = controlador.editarUsuario(1, "n", "a", "d", "e");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void editarUsuarioInexistenteDevuelveElStatusDeLaExcepcion() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "no existe"))
                .when(usuarioService).editarUsuario(1, "n", "a", "d", "e");

        ResponseEntity<String> response = controlador.editarUsuario(1, "n", "a", "d", "e");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("no existe");
    }

    @Test
    void bloquearCuentaOk() {
        ResponseEntity<String> response = controlador.bloquearCuenta(1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(usuarioService).bloquearCuenta(1);
    }

    @Test
    void addLicenciaOk() {
        Licencia licencia = new Licencia();
        ResponseEntity<String> response = controlador.crearLicencia(licencia);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void addLicenciaConErrorDevuelveBadRequest() {
        Licencia licencia = new Licencia();
        doThrow(new IllegalArgumentException("El número de licencia ya existe"))
                .when(licenciaService).registrarLicencia(licencia);

        ResponseEntity<String> response = controlador.crearLicencia(licencia);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void crearServicioOk() {
        Servicio servicio = new Servicio();
        ResponseEntity<String> response = controlador.crearServicio(servicio);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(servicioService).crearServicio(servicio);
    }

    @Test
    void registrarCocheOk() {
        Coche coche = new Coche();
        ResponseEntity<String> response = controlador.registrarCoche(coche);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void registrarCocheConErrorDevuelveBadRequest() {
        Coche coche = new Coche();
        doThrow(new IllegalArgumentException("La matricula ya existe")).when(cocheService).registrarCoche(coche);

        ResponseEntity<String> response = controlador.registrarCoche(coche);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addUsuarioToCocheOk() {
        ResponseEntity<String> response = controlador.addUsuarioToCoche(1, 2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cocheService).addUsuarioToCoche(1, 2);
    }

    @Test
    void addUsuarioToCocheConErrorDevuelveBadRequest() {
        doThrow(new IllegalArgumentException("no existe")).when(cocheService).addUsuarioToCoche(1, 2);

        ResponseEntity<String> response = controlador.addUsuarioToCoche(1, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void usuariosCocheConErrorDevuelveBadRequestConListaVacia() {
        doThrow(new IllegalArgumentException("no existe")).when(cocheService).obtenerUsuariosDeCoche(1);

        ResponseEntity<List<UsuarioDTO>> response = controlador.obtenerUsuariosDeCoche(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void obtenerTaxistasDelegaEnElServicio() {
        List<Usuario> taxistas = List.of(new Usuario());
        when(usuarioService.obtenerTaxistas()).thenReturn(taxistas);

        ResponseEntity<List<Usuario>> response = controlador.obtenerTaxistas();

        assertThat(response.getBody()).isEqualTo(taxistas);
    }
}
