package com.example.microservicio_administrador.controller;

import com.example.microservicio_administrador.dto.TarifaDto;
import com.example.microservicio_administrador.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administradores/tarifas")
public class TarifaController {

    @Autowired
    TarifaService tarifaService;

    @Operation(summary = "Obtener tarifa por tipo", description = "Obtiene una tarifa específica por su tipo")
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> getTarifaVigenteByTipo(@PathVariable String tipo) {
        try {
            TarifaDto tarifaDto = tarifaService.getTarifaVigenteByTipo(tipo);
            return ResponseEntity.status(HttpStatus.OK).body(tarifaDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Obtener todas las tarifas", description = "Devuelve una lista de todas las tarifas disponibles")
    @GetMapping("")
    public ResponseEntity<?> getTarifas() {
        try {
            List<TarifaDto> tarifas = tarifaService.getTarifas();
            return ResponseEntity.status(HttpStatus.OK).body(tarifas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al obtener tarifas.\"}");
        }
    }

    @Operation(summary = "Guardar nueva tarifa", description = "Guarda una nueva tarifa en el sistema")
    @PostMapping("")
    public ResponseEntity<?> save(@RequestBody TarifaDto tarifaDto) {
        try {
            TarifaDto tarifa = tarifaService.save(tarifaDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(tarifa);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Actualizar tarifa", description = "Actualiza una tarifa existente por su ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TarifaDto tarifaDto) {
        try {
            TarifaDto tarifa = tarifaService.update(id, tarifaDto);
            return ResponseEntity.status(HttpStatus.OK).body(tarifa);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Eliminar tarifa", description = "Elimina una tarifa por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            if (tarifaService.delete(id)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Tarifa no encontrada.\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Obtener tarifas normales", description = "Devuelve una lista de tarifas normales")
    @GetMapping("/normales")
    public ResponseEntity<?> getTarifasNormales() {
        try {
            List<TarifaDto> tarifas = tarifaService.getTarifas();
            return ResponseEntity.status(HttpStatus.OK).body(tarifas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error al obtener tarifas.\"}");
        }
    }
}
