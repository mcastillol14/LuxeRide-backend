package com.luxeride.taxistfg.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CocheDTO {
    private Integer id;
    private String modelo;
    private String marca;
    private String matricula;
    private LicenciaDTO licencia;
    private List<UsuarioDTO> usuarios;
    private boolean disponible;
    private UsuarioDTO taxistaEnServicio;
}