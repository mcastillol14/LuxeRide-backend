package com.luxeride.taxistfg.controller;

import com.luxeride.taxistfg.entity.*;
import com.luxeride.taxistfg.dto.*;
import com.luxeride.taxistfg.service.CocheService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.luxeride.taxistfg.service.UsuarioService;

import com.luxeride.taxistfg.service.ServicioService;
import com.luxeride.taxistfg.service.LicenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ControladorAdmin {
    private final CocheService cocheService;
    private final UsuarioService usuarioService;
    private final LicenciaService licenciaService;
    private final ServicioService servicioService;

    //  Usuarios
    @GetMapping("/allUsuarios")
    public Page<UsuarioDTO> obtenerUsuarios(Pageable pageable, @RequestParam(required = false) String dni) {
        return usuarioService.obtenerUsuariosDTOPorFiltro(pageable, dni);
    }

    @PutMapping("/editarUsuario/{id}")
    public ResponseEntity<String> editarUsuario(
            @PathVariable Integer id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String email) {
        try {
            usuarioService.editarUsuario(id, nombre, apellidos, dni, email);
            return ResponseEntity.ok("Usuario editado correctamente.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al editar usuario.");
        }
    }

    @PutMapping("/bloquearCuenta/{id}")
    public ResponseEntity<String> bloquearCuenta(@PathVariable Integer id) {
        try {
            usuarioService.bloquearCuenta(id);
            return ResponseEntity.ok("Cuenta bloqueada correctamente.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al bloquear la cuenta.");
        }
    }

    @PutMapping("/desbloquearCuenta/{id}")
    public ResponseEntity<String> desbloquearCuenta(@PathVariable Integer id) {
        try {
            usuarioService.desbloquearCuenta(id);
            return ResponseEntity.ok("Cuenta desbloqueada correctamente.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al desbloquear la cuenta.");
        }
    }

    @PutMapping("/addTaxista/{id}")
    public ResponseEntity<String> addTaxista(@PathVariable Integer id) {
        try {
            usuarioService.addTaxista(id);
            return ResponseEntity.ok("El usuario ahora es un taxista.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al asignar el rol de taxista.");
        }
    }

    @PutMapping("/deleteTaxista/{id}")
    public ResponseEntity<String> deleteTaxista(@PathVariable Integer id) {
        try {
            usuarioService.deleteTaxista(id);
            return ResponseEntity.ok("El rol de taxista ha sido eliminado del usuario.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al eliminar el rol de taxista.");
        }
    }

    //  Licencias
    @PostMapping("/addLicencia")
    public ResponseEntity<String> crearLicencia(@RequestBody Licencia licencia) {
        try {
            licenciaService.registrarLicencia(licencia);
            return ResponseEntity.ok("Licencia creada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteLicencia/{id}")
    public ResponseEntity<String> borrarLicencia(@PathVariable Integer id) {
        try {
            licenciaService.eliminarLicencia(id);
            return ResponseEntity.ok("Licencia eliminada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/editarLicencia/{id}")
    public ResponseEntity<String> editarLicencia(@PathVariable Integer id, @RequestBody Licencia licencia) {
        try {
            licenciaService.editarLicencia(id, licencia);
            return ResponseEntity.ok("Licencia editada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/allLicencias")
    public Page<Licencia> obtenerLicencias(Pageable pageable, @RequestParam(required = false) String numero) {
        return licenciaService.obtenerLicenciasPorFiltro(pageable, numero);
    }

    // Servicios
    @PostMapping("/addServicio")
    public ResponseEntity<String> crearServicio(@RequestBody Servicio servicio) {
        try {
            servicioService.crearServicio(servicio);
            return ResponseEntity.ok("Servicio creado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/allServicios")
    public Page<Servicio> obtenerServicios(Pageable pageable, @RequestParam(required = false) String tipo) {
        return servicioService.obtenerServiciosPage(tipo, pageable);
    }

    @DeleteMapping("/deleteServicio/{id}")
    public ResponseEntity<String> borrarServicio(@PathVariable Integer id) {
        try {
            servicioService.borrarServicio(id);
            return ResponseEntity.ok("Servicio eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Coches
    @PostMapping("/addCoche")
    public ResponseEntity<String> registrarCoche(@RequestBody Coche coche) {
        try {
            cocheService.registrarCoche(coche);
            return ResponseEntity.ok("Coche registrado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/allCoches")
    public Page<CocheDTO> obtenerCoches(Pageable pageable, @RequestParam(required = false) String matricula) {
        return cocheService.listarCochesDTO(pageable, matricula);
    }

    @PutMapping("/addLicenciaToCoche/{cocheId}/{licenciaId}")
    public ResponseEntity<String> addLicenciaToCoche(@PathVariable Integer cocheId, @PathVariable Integer licenciaId) {
        try {
            cocheService.addLicenciaToCoche(cocheId, licenciaId);
            return ResponseEntity.ok("Licencia asignada al coche exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/eliminarCoche/{id}")
    public ResponseEntity<String> eliminarCoche(@PathVariable Integer id) {
        try {
            cocheService.eliminarCoche(id);
            return ResponseEntity.ok("Coche eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/addUsuarioToCoche/{cocheId}/{usuarioId}")
    public ResponseEntity<String> addUsuarioToCoche(@PathVariable Integer cocheId, @PathVariable Integer usuarioId) {
        try {
            cocheService.addUsuarioToCoche(cocheId, usuarioId);
            return ResponseEntity.ok("Usuario asignado al coche exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteUsuarioToCoche/{cocheId}/{usuarioId}")
    public ResponseEntity<String> deleteUsuarioToCoche(@PathVariable Integer cocheId, @PathVariable Integer usuarioId) {
        try {
            cocheService.deleteUsuarioToCoche(cocheId, usuarioId);
            return ResponseEntity.ok("Usuario eliminado del coche exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuariosCoche/{id}")
    public ResponseEntity<List<UsuarioDTO>> obtenerUsuariosDeCoche(@PathVariable Integer id) {
        try {
            List<UsuarioDTO> usuarios = cocheService.obtenerUsuariosDeCoche(id);
            return ResponseEntity.ok(usuarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/cochesDisponibles")
    public ResponseEntity<List<Coche>> obtenerCochesDisponibles() {
        List<Coche> cochesDisponibles = cocheService.listarCochesDisponibles();
        return ResponseEntity.ok(cochesDisponibles);
    }

    @GetMapping("/cochesNoDisponibles")
    public ResponseEntity<List<Coche>> obtenerCochesNoDisponibles() {
        List<Coche> cochesDisponibles = cocheService.listarCochesNoDisponibles();
        return ResponseEntity.ok(cochesDisponibles);
    }
    @GetMapping("/obtenerTaxistas")
    public ResponseEntity<List<Usuario>>obtenerTaxistas(){
        List<Usuario> taxistas=usuarioService.obtenerTaxistas();
        return ResponseEntity.ok(taxistas);
    }





}
