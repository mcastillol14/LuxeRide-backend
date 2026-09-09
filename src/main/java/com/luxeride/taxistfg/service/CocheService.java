package com.luxeride.taxistfg.service;

import com.luxeride.taxistfg.entity.*;
import com.luxeride.taxistfg.dto.*;
import com.luxeride.taxistfg.repository.CocheRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.luxeride.taxistfg.repository.UsuarioRepository;
import com.luxeride.taxistfg.repository.LicenciaRepository;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CocheService {
    private final CocheRepository cocheRepository;
    private final UsuarioRepository usuarioRepository;
    private final LicenciaRepository licenciaRepository;

    @Transactional
    public void registrarCoche(Coche coche) {
        Optional<Coche> existeCoche = cocheRepository.findByMatricula(coche.getMatricula());
        if (existeCoche.isPresent()) {
            throw new IllegalArgumentException("La matricula ya existe");
        }
        if (coche.getMatricula() == null || coche.getMatricula().isEmpty()) {
            throw new IllegalArgumentException("La matricula es obligatoria");
        }
        if (coche.getMarca() == null || coche.getMarca().isEmpty()) {
            throw new IllegalArgumentException("La marca es obligatoria");
        }
        if (coche.getModelo() == null || coche.getModelo().isEmpty()) {
            throw new IllegalArgumentException("El modelo es obligatorio");
        }
        Coche cocheCreado = new Coche();
        cocheCreado.setMatricula(coche.getMatricula());
        cocheCreado.setMarca(coche.getMarca());
        cocheCreado.setModelo(coche.getModelo());
        cocheCreado.setDisponible(true);
        cocheRepository.save(cocheCreado);
    }

    public CocheDTO cocheACocheDTO(Coche coche) {
        if (coche == null) {
            return null;
        }

        List<UsuarioDTO> usuarioDTOs = coche.getUsuarios() != null ?
                coche.getUsuarios().stream()
                        .map(this::usuarioAUsuarioDTO)
                        .collect(Collectors.toList()) :
                null;

        return new CocheDTO(
                coche.getId(),
                coche.getModelo(),
                coche.getMarca(),
                coche.getMatricula(),
                licenciaALicenciaDTO(coche.getLicencia()),
                usuarioDTOs,
                coche.isDisponible(),
                usuarioAUsuarioDTO(coche.getTaxistaEnServicio())
        );
    }

    private LicenciaDTO licenciaALicenciaDTO(Licencia licencia) {
        if (licencia == null) {
            return null;
        }
        return new LicenciaDTO(
                licencia.getId(),
                licencia.getNumero()
        );
    }

    private UsuarioDTO usuarioAUsuarioDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.isAccountNonLocked()
        );
    }


    public Page<CocheDTO> pageCochesDTO(Page<Coche> cochesPage) {
        List<CocheDTO> cocheDTOs = cochesPage.getContent()
                .stream()
                .map(this::cocheACocheDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(
                cocheDTOs,
                cochesPage.getPageable(),
                cochesPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Page<CocheDTO> listarCochesDTO(Pageable pageable, String matricula) {
        Page<Coche> coches;
        if (matricula != null && !matricula.isEmpty()) {
            coches = cocheRepository.buscarCochesPorMatricula(matricula, pageable);
        } else {
            coches = cocheRepository.findAll(pageable);
        }
        return pageCochesDTO(coches);
    }

    @Transactional(readOnly = true)
    public List<Coche> listarCochesDisponibles() {
        return cocheRepository.findByDisponibleTrue();
    }

    @Transactional(readOnly = true)
    public List<Coche> listarCochesNoDisponibles() {
        return cocheRepository.findByDisponibleFalse();
    }


    @Transactional
    public void eliminarCoche(Integer id) {
        if (!cocheRepository.existsById(id)) {
            throw new IllegalArgumentException("La matricula no existe");
        }
        cocheRepository.deleteById(id);
    }

    @Transactional
    public void addUsuarioToCoche(Integer cocheId, Integer usuarioId) {
        Coche coche = getCocheById(cocheId);
        Usuario usuario = getUsuarioById(usuarioId);

        if (usuario.getRol() != Rol.ROL_TAXISTA) {
            throw new IllegalArgumentException("El usuario no tiene el rol de TAXISTA");
        }

        coche.addUsuario(usuario);
        cocheRepository.save(coche);
    }

    @Transactional
    public void deleteUsuarioToCoche(Integer cocheId, Integer usuarioId) {
        Coche coche = getCocheById(cocheId);
        Usuario usuario = getUsuarioById(usuarioId);

        if (!coche.getUsuarios().contains(usuario)) {
            throw new IllegalArgumentException("El usuario no está asignado a este coche");
        }

        coche.removeUsuario(usuario);
        cocheRepository.save(coche);
    }

    @Transactional
    public void addLicenciaToCoche(Integer cocheId, Integer licenciaId) {
        Coche coche = getCocheById(cocheId);
        Licencia licencia = getLicenciaById(licenciaId);

        if (licencia.getCoche() != null) {
            throw new IllegalArgumentException("La licencia ya está asignada a otro coche");
        }

        coche.setLicencia(licencia);
        licencia.setCoche(coche);

        cocheRepository.save(coche);
        licenciaRepository.save(licencia);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerUsuariosDeCoche(Integer cocheId) {
        Coche coche = getCocheById(cocheId);
        return coche.getUsuarios().stream()
                .map(this::usuarioAUsuarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> ponerCocheEnServicio(Integer cocheId, Integer taxistaId) {
        Coche coche = getCocheById(cocheId);
        Usuario taxista = getUsuarioById(taxistaId);

        if (coche.isEnServicio()) {
            throw new IllegalArgumentException("El coche ya está en servicio");
        }

        if (taxista.getRol() != Rol.ROL_TAXISTA) {
            throw new IllegalArgumentException("El usuario no tiene el rol de TAXISTA");
        }

        if (!coche.getUsuarios().contains(taxista)) {
            throw new IllegalArgumentException("El taxista no está asignado a este coche");
        }

        coche.setEnServicio(true);
        coche.setTaxistaEnServicio(taxista);
        coche.setDisponible(false);
        cocheRepository.save(coche);

        Map<String, Object> response = new HashMap<>();
        response.put("id", taxista.getId());
        response.put("nombre", taxista.getNombre());
        response.put("apellidos", taxista.getApellidos());

        return response;
    }



    @Transactional
    public void liberarCocheDeServicio(Integer cocheId) {
        Coche coche = getCocheById(cocheId);

        if (!coche.isEnServicio()) {
            throw new IllegalArgumentException("El coche no está en servicio");
        }

        coche.setEnServicio(false);
        coche.setTaxistaEnServicio(null);
        coche.setDisponible(true);

        cocheRepository.save(coche);
    }

    @Transactional(readOnly = true)
    public List<Coche> obtenerCochesDisponiblesPorTaxista(Integer taxistaId) {
        Usuario taxista = getUsuarioById(taxistaId);

        if (taxista.getRol() != Rol.ROL_TAXISTA) {
            throw new IllegalArgumentException("El usuario con el ID proporcionado no es un taxista.");
        }

        return cocheRepository.fnindCochesDeTaxista(taxista);
    }

    private Coche getCocheById(Integer id) {
        return cocheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El coche con el ID proporcionado no existe"));
    }

    private Usuario getUsuarioById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario con el ID proporcionado no existe"));
    }

    private Licencia getLicenciaById(Integer id) {
        return licenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La licencia con el ID proporcionado no existe"));
    }

    @Transactional(readOnly = true)
    public List<CocheDTO> listarCochesEnServicio() {
        List<Coche> cochesEnServicio = cocheRepository.findByEnServicioTrue();

        return cochesEnServicio.stream()
                .map(this::cocheACocheDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CocheDTO obtenerCocheEnServicioPorId(Integer cocheId) {
        if (cocheId == null) {
            throw new IllegalArgumentException("El ID del coche no puede ser nulo");
        }

        Optional<Coche> cocheOptional = cocheRepository.findByIdAndEnServicioTrue(cocheId);

        if (cocheOptional.isPresent()) {
            return cocheACocheDTO(cocheOptional.get());
        } else {
            return null;
        }
    }

}