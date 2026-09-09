package com.luxeride.taxistfg.Service;

import com.luxeride.taxistfg.Model.Servicio;
import com.luxeride.taxistfg.Repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    private ServicioService servicioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servicioService = new ServicioService();
        ReflectionTestUtils.setField(servicioService, "servicioRepository", servicioRepository);
    }

    private Servicio servicioCompleto() {
        Servicio servicio = new Servicio();
        servicio.setTipo("Estandar");
        servicio.setDescripcion("Servicio estandar");
        servicio.setPrecioPorKm(BigDecimal.ONE);
        return servicio;
    }

    @Test
    void crearServicioOk() {
        Servicio servicio = servicioCompleto();
        when(servicioRepository.findByTipo("Estandar")).thenReturn(Optional.empty());

        servicioService.crearServicio(servicio);

        verify(servicioRepository).save(servicio);
    }

    @Test
    void crearServicioConTipoExistenteLanzaExcepcion() {
        Servicio servicio = servicioCompleto();
        when(servicioRepository.findByTipo("Estandar")).thenReturn(Optional.of(new Servicio()));

        assertThatThrownBy(() -> servicioService.crearServicio(servicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya existe");
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void crearServicioSinDescripcionLanzaExcepcion() {
        Servicio servicio = servicioCompleto();
        servicio.setDescripcion(" ");
        when(servicioRepository.findByTipo("Estandar")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.crearServicio(servicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descripción");
    }

    @Test
    void crearServicioSinPrecioLanzaExcepcion() {
        Servicio servicio = servicioCompleto();
        servicio.setPrecioPorKm(null);
        when(servicioRepository.findByTipo("Estandar")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.crearServicio(servicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precio");
    }

    @Test
    void borrarServicioOk() {
        when(servicioRepository.existsById(1)).thenReturn(true);

        servicioService.borrarServicio(1);

        verify(servicioRepository).deleteById(1);
    }

    @Test
    void borrarServicioInexistenteLanzaExcepcion() {
        when(servicioRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> servicioService.borrarServicio(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerServiciosPageSinTipoUsaFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Servicio> page = new PageImpl<>(List.of(servicioCompleto()));
        when(servicioRepository.findAll(pageable)).thenReturn(page);

        assertThat(servicioService.obtenerServiciosPage(null, pageable)).isEqualTo(page);
    }

    @Test
    void obtenerServiciosPageConTipoUsaBusqueda() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Servicio> page = new PageImpl<>(List.of(servicioCompleto()));
        when(servicioRepository.buscarServiciosPorTipo("Est", pageable)).thenReturn(page);

        assertThat(servicioService.obtenerServiciosPage("Est", pageable)).isEqualTo(page);
        verify(servicioRepository, never()).findAll(pageable);
    }

    @Test
    void obtenerServiciosDelegaEnFindAll() {
        List<Servicio> servicios = List.of(servicioCompleto());
        when(servicioRepository.findAll()).thenReturn(servicios);

        assertThat(servicioService.obtenerServicios()).isEqualTo(servicios);
    }
}
