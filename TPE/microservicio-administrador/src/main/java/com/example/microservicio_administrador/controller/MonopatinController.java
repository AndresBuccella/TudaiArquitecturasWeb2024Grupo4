package com.example.microservicio_administrador.controller;

import com.example.microservicio_administrador.feignClient.MonopatinFeignClient;
import com.example.microservicio_administrador.model.Monopatin;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administradores/monopatines")
public class MonopatinController {

    @Autowired
    private MonopatinFeignClient monopatinFeignClient;

    /*
    3e. Como administrador quiero consultar la cantidad de monopatines actualmente en operación,
    versus la cantidad de monopatines actualmente en mantenimiento.
    */

    @Operation(summary = "Permite al adm consultar monopatines en operación", description = "Consulta la cantidad de monopatines en operación y mantenimiento")
    @GetMapping("/enOperacion")
    public ResponseEntity<String> consultarEnOperacion() {
        List<Monopatin> monopatines = monopatinFeignClient.getAll();

        if (monopatines == null || monopatines.isEmpty()) {
            return ResponseEntity.ok("No hay monopatines disponibles.");
        }

        long enOperacion = monopatines.stream()
                .filter(monopatin -> Boolean.TRUE.equals(monopatin.getDisponible()) ||
                        (Boolean.FALSE.equals(monopatin.getDisponible()) && monopatin.getViajeActivo() != null))
                .count();

        long enMantenimiento = monopatines.size() - enOperacion;

        String resultado = String.format("Monopatines en operación: %d, Monopatines en mantenimiento: %d", enOperacion, enMantenimiento);
        return ResponseEntity.ok(resultado);
    }


    @Operation(summary = "Permite al adm añadir monopatín", description = "Añade un nuevo monopatín")
    @PostMapping("")
    public ResponseEntity<Monopatin> addMonopatin(@RequestBody Monopatin newMonopatin) {
        Monopatin monopatin = monopatinFeignClient.save(newMonopatin);
        if (monopatin == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(monopatin);
    }

    @Operation(summary = "Permite al adm habilitar monopatín", description = "Habilita un monopatín por su ID")
    @PutMapping("/habilitar/{id}")
    public ResponseEntity<Void> habilitarMonopatin(@PathVariable Long id) {
        boolean exito = monopatinFeignClient.habilitarMonopatin(id);
        if (!exito) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Permite al adm deshabilitar monopatín", description = "Deshabilita un monopatín por su ID")
    @PutMapping("/deshabilitar/{id}")
    public ResponseEntity<Void> deshabilitarMonopatin(@PathVariable Long id) {
        boolean exito = monopatinFeignClient.deshabilitarMonopatin(id);
        if (!exito) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Permite al adm actualizar monopatín", description = "Actualiza un monopatín por su ID")
    @PutMapping("/{id}")
    public ResponseEntity<Monopatin> updateMonopatin(@PathVariable Long id, @RequestBody Monopatin newMonopatin) {
        Monopatin monopatin = monopatinFeignClient.updateMonopatin(id, newMonopatin);
        if (monopatin == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(monopatin);
    }

}
