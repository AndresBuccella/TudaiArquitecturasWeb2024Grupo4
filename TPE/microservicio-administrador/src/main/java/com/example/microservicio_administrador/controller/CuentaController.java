package com.example.microservicio_administrador.controller;

import com.example.microservicio_administrador.feignClient.CuentaFeignClient;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administradores/cuentas")
public class CuentaController {
    @Autowired
    private CuentaFeignClient cuentaFeignClient;

    /*
    3b. Como administrador quiero poder anular cuentas para inhabilitar el uso momentáneo de la misma.
    */
    @Operation(summary = "Pemite al adm habilitar cuenta", description = "Habilita una cuenta de usuario por su ID")
    @PutMapping("/habilitar/{id}")
    public ResponseEntity<?> habilitarCuenta(@PathVariable Long id) {
        try {
            return cuentaFeignClient.habilitarCuenta(id);
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Permite al adm deshabilitar cuenta", description = "Deshabilita una cuenta de usuario por su ID")
    @PutMapping("/deshabilitar/{id}")
    public ResponseEntity<?> deshabilitarCuenta(@PathVariable Long id) {
        try {
            return cuentaFeignClient.deshabilitarCuenta(id);
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
