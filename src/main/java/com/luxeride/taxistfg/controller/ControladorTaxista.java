package com.luxeride.taxistfg.controller;

import com.luxeride.taxistfg.entity.Coche;
import com.luxeride.taxistfg.dto.CocheDTO;
import com.luxeride.taxistfg.entity.Usuario;
import com.luxeride.taxistfg.service.CocheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/taxista")
@RequiredArgsConstructor
public class ControladorTaxista {

    private final CocheService cocheService;
    @PutMapping("/ponerEnServicio/{cocheId}/{taxistaId}")
    public ResponseEntity<Map<String, Object>> ponerEnServicio(@PathVariable Integer cocheId, @PathVariable Integer taxistaId) {
        try {
            Map<String, Object> taxistaInfo = cocheService.ponerCocheEnServicio(cocheId, taxistaId);
            return ResponseEntity.ok(taxistaInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }




    @PutMapping("/liberarCoche/{id}")
    public ResponseEntity<String> liberarCoche(@PathVariable Integer id) {
        try {
            cocheService.liberarCocheDeServicio(id);
            return ResponseEntity.ok("Coche liberado y ahora está disponible.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al liberar el coche.");
        }
    }
    @GetMapping("/cochesTaxistas/{taxistaId}")
    public ResponseEntity<List<CocheDTO>> obtenerCochesDisponiblesPorTaxista(@PathVariable Integer taxistaId) {
        try {
            List<Coche> coches = cocheService.obtenerCochesDisponiblesPorTaxista(taxistaId);
            List<CocheDTO> cocheDTOs = coches.stream()
                    .map(cocheService::cocheACocheDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(cocheDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
