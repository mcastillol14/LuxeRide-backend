package com.luxeride.taxistfg.repository;

import com.luxeride.taxistfg.entity.Servicio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    Optional<Servicio> findByTipo(String tipo);

    @Query("SELECT s FROM Servicio s WHERE s.tipo LIKE %:tipo%")
    Page<Servicio> buscarServiciosPorTipo(@Param("tipo") String tipo, Pageable pageable);

    void deleteById(Integer id);

    Page<Servicio> findAll(Pageable pageable);

}