package com.example.microservicio_usuario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String apellido;
    private String nroCelular;
    private String email;
    private Boolean habilitado;

    private String username;
    private Long idMonopatinActual;

    @ElementCollection
    @CollectionTable(name = "usuarioCuentaApps", joinColumns = @JoinColumn(name = "usuarioId"))
    @Column(name = "idCuentaApp")
    private List<Long> idCuentaApps;


    // Método para agregar una cuenta por su ID
    public void agregarCuentaApp(Long cuentaId) {
        if (!idCuentaApps.contains(cuentaId)) {
            idCuentaApps.add(cuentaId);
        }
    }

    // Método para quitar una cuenta por su ID
    public void quitarCuentaApp(Long cuentaId) {
        idCuentaApps.remove(cuentaId);
    }
}
