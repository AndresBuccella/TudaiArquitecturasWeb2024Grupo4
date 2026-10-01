package com.example.microservicio_administrador.controller;


import com.example.microservicio_administrador.dto.AdministradorDto;
import com.example.microservicio_administrador.model.ReporteTotalFacturadoEntreMesesDeAnio;
import com.example.microservicio_administrador.service.AdministradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;


    @Operation(summary = "Obtener todos los administradores", description = "Devuelve una lista de todos los administradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de administradores obtenida con éxito"),
            @ApiResponse(responseCode = "204", description = "No hay administradores disponibles")
    })
    @GetMapping("")
    public ResponseEntity<List<AdministradorDto>> getAllAdministradores() {
        List<AdministradorDto> administradores = administradorService.getAllAdministradores();
        if (administradores.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(administradores);
    }


    @Operation(summary = "Obtener un administrador por ID", description = "Devuelve un administrador basado en el ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Administrador obtenido con éxito"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdministradorDto> getAdministradorById(@PathVariable Long id) {
        AdministradorDto admin = administradorService.getAdministradorById(id);
        if (admin == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(admin);
    }

    @Operation(summary = "Actualizar un administrador por ID", description = "Actualiza un administrador basado en el ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Administrador actualizado con éxito"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdministradorDto> updateAdministrador(@PathVariable Long id, AdministradorDto admin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Operation(summary = "Guardar un nuevo administrador", description = "Crea un nuevo administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Administrador creado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o error al crear el administrador")
    })
    @PostMapping
    public ResponseEntity<AdministradorDto> save(@RequestBody AdministradorDto newAdmin) {
        AdministradorDto admin = administradorService.save(newAdmin);

        if (admin == null)
            return ResponseEntity.badRequest().build();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(admin.getId())
                .toUri();

        return ResponseEntity.created(location).body(admin);
    }

    @Operation(summary = "Eliminar un administrador", description = "Elimina un administrador basado en el ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Administrador eliminado con éxito"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        administradorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener reporte total facturado entre meses de un año", description = "Devuelve el reporte de facturación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte obtenido con éxito"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado o error en la solicitud")
    })
    @GetMapping("/reporteTotalFacturadoEntreMesesDeAnio")
    public ResponseEntity<?> getReporteTotalFacturadoEntreMesesDeAnio(
            @RequestParam Long mesInicio,
            @RequestParam Long mesFin,
            @RequestParam Long anio) {
        try {
            ReporteTotalFacturadoEntreMesesDeAnio reporte = administradorService.getReporteTotalFacturadoEntreMesesDeAnio(mesInicio, mesFin, anio);
            return ResponseEntity.status(HttpStatus.OK).body(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
