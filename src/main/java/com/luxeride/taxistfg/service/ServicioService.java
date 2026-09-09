package com.luxeride.taxistfg.service;

import com.luxeride.taxistfg.entity.Servicio;
import com.luxeride.taxistfg.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    @Transactional
    public void crearServicio(Servicio servicio) {
        if (servicioRepository.findByTipo(servicio.getTipo()).isPresent()) {
            throw new IllegalArgumentException("Este tipo de servicio ya existe");
        }

        if (servicio.getTipo() == null || servicio.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }
        if (servicio.getDescripcion() == null || servicio.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        if (servicio.getPrecioPorKm() == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }

        servicioRepository.save(servicio);
    }

    @Transactional
    public void borrarServicio(Integer id) {
        if (!servicioRepository.existsById(id)) {
            throw new IllegalArgumentException("El servicio no existe");
        }
        servicioRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public Page<Servicio> obtenerServiciosPage(String tipo, Pageable pageable) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return servicioRepository.findAll(pageable);
        }
        return servicioRepository.buscarServiciosPorTipo(tipo, pageable);
    }

    @Transactional(readOnly = true)
    public List<Servicio> obtenerServicios(){
        return servicioRepository.findAll();
     }
}
