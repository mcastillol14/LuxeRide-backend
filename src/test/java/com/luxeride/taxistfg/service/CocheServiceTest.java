package com.luxeride.taxistfg.service;

import com.luxeride.taxistfg.entity.*;
import com.luxeride.taxistfg.dto.*;
import com.luxeride.taxistfg.repository.CocheRepository;
import com.luxeride.taxistfg.repository.LicenciaRepository;
import com.luxeride.taxistfg.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CocheServiceTest {

    @Mock
    private CocheRepository cocheRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private LicenciaRepository licenciaRepository;

    private CocheService cocheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cocheService = new CocheService(cocheRepository, usuarioRepository, licenciaRepository);
    }

    private Coche cocheCompleto() {
        Coche coche = new Coche();
        coche.setMatricula("1234ABC");
        coche.setMarca("Toyota");
        coche.setModelo("Corolla");
        coche.setUsuarios(new HashSet<>());
        return coche;
    }

    @Test
    void registrarCocheOkGuardaDisponible() {
        Coche coche = cocheCompleto();
        when(cocheRepository.findByMatricula("1234ABC")).thenReturn(Optional.empty());

        cocheService.registrarCoche(coche);

        verify(cocheRepository).save(argThat(c -> c.isDisponible() && c.getMatricula().equals("1234ABC")));
    }

    @Test
    void registrarCocheConMatriculaExistenteLanzaExcepcion() {
        Coche coche = cocheCompleto();
        when(cocheRepository.findByMatricula("1234ABC")).thenReturn(Optional.of(new Coche()));

        assertThatThrownBy(() -> cocheService.registrarCoche(coche))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("matricula");
    }

    @Test
    void registrarCocheSinMarcaLanzaExcepcion() {
        Coche coche = cocheCompleto();
        coche.setMarca(null);
        when(cocheRepository.findByMatricula("1234ABC")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cocheService.registrarCoche(coche))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marca");
    }

    @Test
    void registrarCocheSinModeloLanzaExcepcion() {
        Coche coche = cocheCompleto();
        coche.setModelo("");
        when(cocheRepository.findByMatricula("1234ABC")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cocheService.registrarCoche(coche))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("modelo");
    }

    @Test
    void eliminarCocheOk() {
        when(cocheRepository.existsById(1)).thenReturn(true);

        cocheService.eliminarCoche(1);

        verify(cocheRepository).deleteById(1);
    }

    @Test
    void eliminarCocheInexistenteLanzaExcepcion() {
        when(cocheRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> cocheService.eliminarCoche(1))
                .isInstanceOf(IllegalArgumentException.class);
        verify(cocheRepository, never()).deleteById(any());
    }

    @Test
    void addUsuarioToCocheOkCuandoEsTaxista() {
        Coche coche = cocheCompleto();
        Usuario taxista = new Usuario();
        taxista.setRol(Rol.ROL_TAXISTA);
        taxista.setCoches(new HashSet<>());
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(taxista));

        cocheService.addUsuarioToCoche(1, 2);

        assertThat(coche.getUsuarios()).contains(taxista);
        verify(cocheRepository).save(coche);
    }

    @Test
    void addUsuarioToCocheConUsuarioNoTaxistaLanzaExcepcion() {
        Coche coche = cocheCompleto();
        Usuario cliente = new Usuario();
        cliente.setRol(Rol.ROL_CLIENTE);
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> cocheService.addUsuarioToCoche(1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TAXISTA");
    }

    @Test
    void deleteUsuarioToCocheNoAsignadoLanzaExcepcion() {
        Coche coche = cocheCompleto();
        Usuario usuario = new Usuario();
        usuario.setId(5);
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> cocheService.deleteUsuarioToCoche(1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asignado");
    }

    @Test
    void addLicenciaToCocheOk() {
        Coche coche = cocheCompleto();
        Licencia licencia = new Licencia();
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(licenciaRepository.findById(2)).thenReturn(Optional.of(licencia));

        cocheService.addLicenciaToCoche(1, 2);

        assertThat(coche.getLicencia()).isEqualTo(licencia);
        assertThat(licencia.getCoche()).isEqualTo(coche);
        verify(cocheRepository).save(coche);
        verify(licenciaRepository).save(licencia);
    }

    @Test
    void addLicenciaToCocheYaAsignadaLanzaExcepcion() {
        Coche coche = cocheCompleto();
        Licencia licencia = new Licencia();
        licencia.setCoche(new Coche());
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(licenciaRepository.findById(2)).thenReturn(Optional.of(licencia));

        assertThatThrownBy(() -> cocheService.addLicenciaToCoche(1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asignada");
    }

    @Test
    void ponerCocheEnServicioOk() {
        Coche coche = cocheCompleto();
        Usuario taxista = new Usuario();
        taxista.setId(9);
        taxista.setNombre("Pepe");
        taxista.setApellidos("Gomez");
        taxista.setRol(Rol.ROL_TAXISTA);
        coche.getUsuarios().add(taxista);
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(usuarioRepository.findById(9)).thenReturn(Optional.of(taxista));

        Map<String, Object> response = cocheService.ponerCocheEnServicio(1, 9);

        assertThat(coche.isEnServicio()).isTrue();
        assertThat(coche.isDisponible()).isFalse();
        assertThat(response.get("id")).isEqualTo(9);
        assertThat(response.get("nombre")).isEqualTo("Pepe");
    }

    @Test
    void ponerCocheEnServicioYaEnServicioLanzaExcepcion() {
        Coche coche = cocheCompleto();
        coche.setEnServicio(true);
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(usuarioRepository.findById(9)).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> cocheService.ponerCocheEnServicio(1, 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está en servicio");
    }

    @Test
    void ponerCocheEnServicioTaxistaNoAsignadoLanzaExcepcion() {
        Coche coche = cocheCompleto();
        Usuario taxista = new Usuario();
        taxista.setId(9);
        taxista.setRol(Rol.ROL_TAXISTA);
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));
        when(usuarioRepository.findById(9)).thenReturn(Optional.of(taxista));

        assertThatThrownBy(() -> cocheService.ponerCocheEnServicio(1, 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está asignado");
    }

    @Test
    void liberarCocheDeServicioOk() {
        Coche coche = cocheCompleto();
        coche.setEnServicio(true);
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));

        cocheService.liberarCocheDeServicio(1);

        assertThat(coche.isEnServicio()).isFalse();
        assertThat(coche.isDisponible()).isTrue();
        assertThat(coche.getTaxistaEnServicio()).isNull();
    }

    @Test
    void liberarCocheQueNoEstaEnServicioLanzaExcepcion() {
        Coche coche = cocheCompleto();
        coche.setEnServicio(false);
        when(cocheRepository.findById(1)).thenReturn(Optional.of(coche));

        assertThatThrownBy(() -> cocheService.liberarCocheDeServicio(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerCochesDisponiblesPorTaxistaOk() {
        Usuario taxista = new Usuario();
        taxista.setRol(Rol.ROL_TAXISTA);
        List<Coche> coches = List.of(cocheCompleto());
        when(usuarioRepository.findById(9)).thenReturn(Optional.of(taxista));
        when(cocheRepository.fnindCochesDeTaxista(taxista)).thenReturn(coches);

        assertThat(cocheService.obtenerCochesDisponiblesPorTaxista(9)).isEqualTo(coches);
    }

    @Test
    void obtenerCochesDisponiblesPorTaxistaConUsuarioNoTaxistaLanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setRol(Rol.ROL_CLIENTE);
        when(usuarioRepository.findById(9)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> cocheService.obtenerCochesDisponiblesPorTaxista(9))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cocheACocheDTOConNullDevuelveNull() {
        assertThat(cocheService.cocheACocheDTO(null)).isNull();
    }

    @Test
    void cocheACocheDTOMapeaLosCampos() {
        Coche coche = cocheCompleto();
        coche.setId(7);

        CocheDTO dto = cocheService.cocheACocheDTO(coche);

        assertThat(dto.getId()).isEqualTo(7);
        assertThat(dto.getMatricula()).isEqualTo("1234ABC");
        assertThat(dto.getMarca()).isEqualTo("Toyota");
        assertThat(dto.getModelo()).isEqualTo("Corolla");
    }

    @Test
    void obtenerCocheEnServicioPorIdConIdNuloLanzaExcepcion() {
        assertThatThrownBy(() -> cocheService.obtenerCocheEnServicioPorId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerCocheEnServicioPorIdNoEncontradoDevuelveNull() {
        when(cocheRepository.findByIdAndEnServicioTrue(1)).thenReturn(Optional.empty());

        assertThat(cocheService.obtenerCocheEnServicioPorId(1)).isNull();
    }
}
