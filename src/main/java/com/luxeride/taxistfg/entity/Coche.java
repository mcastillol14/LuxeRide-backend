package com.luxeride.taxistfg.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coches")
public class Coche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @OneToOne
    @JoinColumn(name = "licencia_id", unique = true)
    @JsonIgnoreProperties("coche")
    private Licencia licencia;

    @Column(nullable = false)
    private boolean disponible = false;

    @Column(nullable = false)
    private boolean enServicio = false;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "coches")
    @JsonIgnoreProperties({"coches", "viajesComoCliente", "viajesComoTaxista"})
    private Set<Usuario> usuarios;

    @OneToMany(mappedBy = "coche")
    private Set<Viaje> viajes;

    // taxista que esta usando el coche ahora mismo, distinto de los usuarios asignados en general (usuarios)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxista_id_servicio")
    private Usuario taxistaEnServicio;

    public void addUsuario(Usuario usuario) {
        this.usuarios.add(usuario);
        usuario.getCoches().add(this);
    }

    public void removeUsuario(Usuario usuario) {
        this.usuarios.remove(usuario);
        usuario.getCoches().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coche coche = (Coche) o;
        return Objects.equals(id, coche.id) &&
                Objects.equals(matricula, coche.matricula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, matricula);
    }
}
