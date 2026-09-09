package com.luxeride.taxistfg.Service;

import com.luxeride.taxistfg.Model.Coche;
import com.luxeride.taxistfg.Model.Servicio;
import com.luxeride.taxistfg.Model.Usuario;
import com.luxeride.taxistfg.Model.Viaje;
import com.luxeride.taxistfg.Repository.ViajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ViajeServiceTest {

    @Mock
    private ViajeRepository viajeRepository;

    private ViajeService viajeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        viajeService = new ViajeService();
        ReflectionTestUtils.setField(viajeService, "viajeRepository", viajeRepository);
    }

    private Viaje viajeCompleto() {
        Viaje viaje = new Viaje();
        viaje.setHoraInicio(LocalDateTime.now());
        viaje.setHoraLlegada(LocalDateTime.now().plusMinutes(20));
        viaje.setOrigen("Madrid");
        viaje.setDestino("Barcelona");
        viaje.setDistanciaKm(BigDecimal.TEN);
        viaje.setPrecioTotal(BigDecimal.valueOf(50));
        viaje.setCliente(new Usuario());
        viaje.setTaxista(new Usuario());
        viaje.setServicio(new Servicio());
        viaje.setCoche(new Coche());
        viaje.setFoto(new byte[]{1, 2, 3});
        return viaje;
    }

    @Test
    void registrarViajeOkGuardaElViaje() {
        Viaje viaje = viajeCompleto();

        viajeService.registrarViaje(viaje);

        verify(viajeRepository).save(viaje);
    }

    @Test
    void registrarViajeNuloLanzaExcepcion() {
        assertThatThrownBy(() -> viajeService.registrarViaje(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");
        verify(viajeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registrarViajeSinHorasLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setHoraInicio(null);

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("horas");
    }

    @Test
    void registrarViajeSinOrigenLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setOrigen("");

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("origen");
    }

    @Test
    void registrarViajeSinDestinoLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setDestino(null);

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("destino");
    }

    @Test
    void registrarViajeSinClienteLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setCliente(null);

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cliente");
    }

    @Test
    void registrarViajeSinTaxistaLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setTaxista(null);

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taxista");
    }

    @Test
    void registrarViajeSinServicioLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setServicio(null);

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("servicio");
    }

    @Test
    void registrarViajeSinCocheLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setCoche(null);

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coche");
    }

    @Test
    void registrarViajeSinFotoLanzaExcepcion() {
        Viaje viaje = viajeCompleto();
        viaje.setFoto(new byte[0]);

        assertThatThrownBy(() -> viajeService.registrarViaje(viaje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foto");
    }
}
