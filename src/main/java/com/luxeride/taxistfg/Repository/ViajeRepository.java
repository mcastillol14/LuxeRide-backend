package com.luxeride.taxistfg.Repository;

import com.luxeride.taxistfg.Model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ViajeRepository extends JpaRepository<Viaje, Integer> {

    // trae solo el viaje mas reciente del cliente en vez de la lista entera, se usa para el pdf del ultimo viaje
    Optional<Viaje> findFirstByClienteIdOrderByIdDesc(Integer idCliente);

}

