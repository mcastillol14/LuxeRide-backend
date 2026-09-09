package com.luxeride.taxistfg.repository;

import com.luxeride.taxistfg.entity.Usuario;
import com.luxeride.taxistfg.entity.Rol;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDni(String dni);

    Page<Usuario> findAll(Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE (:dni IS NULL OR u.dni LIKE %:dni%)")
    Page<Usuario> findByDni(Pageable pageable, String dni);

    List<Usuario> findByRol(Rol rol);

}
