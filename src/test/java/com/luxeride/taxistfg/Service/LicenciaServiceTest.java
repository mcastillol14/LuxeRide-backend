package com.luxeride.taxistfg.Service;

import com.luxeride.taxistfg.Model.Licencia;
import com.luxeride.taxistfg.Repository.LicenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LicenciaServiceTest {

    @Mock
    private LicenciaRepository licenciaRepository;

    private LicenciaService licenciaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        licenciaService = new LicenciaService();
        ReflectionTestUtils.setField(licenciaService, "licenciaRepository", licenciaRepository);
    }

    @Test
    void registrarLicenciaOk() {
        Licencia licencia = new Licencia();
        licencia.setNumero("L-001");
        when(licenciaRepository.findByNumero("L-001")).thenReturn(Optional.empty());

        licenciaService.registrarLicencia(licencia);

        verify(licenciaRepository).save(argThat(l -> l.getNumero().equals("L-001")));
    }

    @Test
    void registrarLicenciaConNumeroExistenteLanzaExcepcion() {
        Licencia licencia = new Licencia();
        licencia.setNumero("L-001");
        when(licenciaRepository.findByNumero("L-001")).thenReturn(Optional.of(new Licencia()));

        assertThatThrownBy(() -> licenciaService.registrarLicencia(licencia))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya existe");
    }

    @Test
    void registrarLicenciaSinNumeroLanzaExcepcion() {
        Licencia licencia = new Licencia();
        licencia.setNumero("");
        when(licenciaRepository.findByNumero("")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licenciaService.registrarLicencia(licencia))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void obtenerLicenciasPorFiltroConNumeroUsaBusqueda() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Licencia> page = new PageImpl<>(List.of(new Licencia()));
        when(licenciaRepository.buscarLicenciasPorNumero("L-", pageable)).thenReturn(page);

        assertThat(licenciaService.obtenerLicenciasPorFiltro(pageable, "L-")).isEqualTo(page);
    }

    @Test
    void obtenerLicenciasPorFiltroSinNumeroUsaFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Licencia> page = new PageImpl<>(List.of(new Licencia()));
        when(licenciaRepository.findAll(pageable)).thenReturn(page);

        assertThat(licenciaService.obtenerLicenciasPorFiltro(pageable, null)).isEqualTo(page);
    }

    @Test
    void eliminarLicenciaOk() {
        when(licenciaRepository.findById(1)).thenReturn(Optional.of(new Licencia()));

        licenciaService.eliminarLicencia(1);

        verify(licenciaRepository).deleteById(1);
    }

    @Test
    void eliminarLicenciaInexistenteLanzaExcepcion() {
        when(licenciaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licenciaService.eliminarLicencia(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void editarLicenciaOkActualizaNumero() {
        Licencia existente = new Licencia();
        existente.setNumero("viejo");
        when(licenciaRepository.findById(1)).thenReturn(Optional.of(existente));

        Licencia cambios = new Licencia();
        cambios.setNumero("nuevo");
        licenciaService.editarLicencia(1, cambios);

        assertThat(existente.getNumero()).isEqualTo("nuevo");
        verify(licenciaRepository).save(existente);
    }

    @Test
    void editarLicenciaInexistenteLanzaExcepcion() {
        when(licenciaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licenciaService.editarLicencia(1, new Licencia()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
