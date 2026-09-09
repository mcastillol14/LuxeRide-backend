package com.luxeride.taxistfg.Service;

import com.luxeride.taxistfg.Model.Servicio;
import com.luxeride.taxistfg.Repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

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
