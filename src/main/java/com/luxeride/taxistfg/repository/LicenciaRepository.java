package com.luxeride.taxistfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.luxeride.taxistfg.entity.Licencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface LicenciaRepository extends JpaRepository<Licencia, Integer> {
    Optional<Licencia> findByNumero(String numero);

    @Query("SELECT l FROM Licencia l WHERE l.numero LIKE %:numero%")
    Page<Licencia> buscarLicenciasPorNumero(@Param("numero") String numero, Pageable pageable);

    Page<Licencia> findAll(Pageable pageable);

    void deleteById(Integer id);
}

