package com.luxeride.taxistfg.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String tipo;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "precio_por_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorKm;

    @OneToMany(mappedBy = "servicio")
    private Set<Viaje> viajes;
}
