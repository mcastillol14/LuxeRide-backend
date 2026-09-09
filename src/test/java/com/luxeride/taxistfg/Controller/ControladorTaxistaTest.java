package com.luxeride.taxistfg.Controller;

import com.luxeride.taxistfg.Model.Coche;
import com.luxeride.taxistfg.Model.CocheDTO;
import com.luxeride.taxistfg.Service.CocheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ControladorTaxistaTest {

    @Mock
    private CocheService cocheService;

    private ControladorTaxista controlador;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // sin @RequiredArgsConstructor ni constructor propio, solo campo @Autowired: se inyecta a mano
        controlador = new ControladorTaxista();
        ReflectionTestUtils.setField(controlador, "cocheService", cocheService);
    }

    @Test
    void ponerEnServicioOk() {
        Map<String, Object> info = Map.of("id", 9, "nombre", "Pepe");
        when(cocheService.ponerCocheEnServicio(1, 9)).thenReturn(info);

        ResponseEntity<Map<String, Object>> response = controlador.ponerEnServicio(1, 9);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(info);
    }

    @Test
    void ponerEnServicioConErrorDevuelveBadRequest() {
        when(cocheService.ponerCocheEnServicio(1, 9)).thenThrow(new IllegalArgumentException("ya en servicio"));

        ResponseEntity<Map<String, Object>> response = controlador.ponerEnServicio(1, 9);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void ponerEnServicioConExcepcionInesperadaDevuelve500() {
        when(cocheService.ponerCocheEnServicio(1, 9)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<Map<String, Object>> response = controlador.ponerEnServicio(1, 9);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void liberarCocheOk() {
        ResponseEntity<String> response = controlador.liberarCoche(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cocheService).liberarCocheDeServicio(1);
    }

    @Test
    void liberarCocheConErrorDevuelveBadRequest() {
        doThrow(new IllegalArgumentException("no esta en servicio")).when(cocheService).liberarCocheDeServicio(1);

        ResponseEntity<String> response = controlador.liberarCoche(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void obtenerCochesDisponiblesPorTaxistaOk() {
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        CocheDTO dto = new CocheDTO();
        dto.setMatricula("1234ABC");
        when(cocheService.obtenerCochesDisponiblesPorTaxista(9)).thenReturn(List.of(coche));
        when(cocheService.cocheACocheDTO(coche)).thenReturn(dto);

        ResponseEntity<List<CocheDTO>> response = controlador.obtenerCochesDisponiblesPorTaxista(9);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(dto);
    }

    @Test
    void obtenerCochesDisponiblesPorTaxistaConErrorDevuelveBadRequestConListaVacia() {
        when(cocheService.obtenerCochesDisponiblesPorTaxista(9))
                .thenThrow(new IllegalArgumentException("no es taxista"));

        ResponseEntity<List<CocheDTO>> response = controlador.obtenerCochesDisponiblesPorTaxista(9);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEmpty();
    }
}
