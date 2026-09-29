package com.example.microservicio_usuario.controller;

import com.example.microservicio_usuario.service.UsuarioService;
import com.example.microservicio_usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;
    // Create

    @Operation(summary = "Crear un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @PostMapping("")
    public ResponseEntity<Usuario> save(@RequestBody Usuario usuario,
                                        @RequestHeader("X-User-Username") String username) {
        System.out.println("AAAAAAAAAAA");
        Usuario usuarioNew = usuarioService.save(usuario, username);
        return ResponseEntity.ok(usuarioNew);
    }

    // Read

    @Operation(summary = "Obtener todos los usuarios")
    @GetMapping("")
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAll();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Obtener todos los usuarios habilitados")
    @GetMapping("/habilitados")
    public ResponseEntity<List<Usuario>> getAllUsuariosHabilitados() {
        List<Usuario> usuarios = usuarioService.getAllHabilitados();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Obtener todos los usuarios deshabilitados")
    @GetMapping("/deshabilitados")
    public ResponseEntity<List<Usuario>> getAllUsuariosDeshabilitados() {
        List<Usuario> usuarios = usuarioService.getAllDeshabilitados();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }


    @Operation(summary = "Obtener un usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable("id") Long id) {
        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<Usuario> getUsuarioByUsername(@PathVariable("username") String username) {
        Usuario usuario = usuarioService.findByUsername(username);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.delete(usuario);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Habilitar un usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario habilitado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    // Habilitar
    @PutMapping("/habilitar/{id}")
    public ResponseEntity<Void> habilitar(@PathVariable("id") Long id) {
        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.habilitar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deshabilitar un usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario deshabilitado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    // Deshabilitar
    @PutMapping("/deshabilitar/{id}")
    public ResponseEntity<Void> deshabilitar(@PathVariable("id") Long id) {
        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deshabilitar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reservar un monopatín")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monopatín reservado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @PutMapping("/reservarMonopatin")
    public ResponseEntity<?> reservarMonopatin(@RequestParam("idCuenta") Long idCuenta,
                                               @RequestParam("idParada") Long idParada,
                                               @RequestParam("idMonopatin") Long idMonopatin) {
        usuarioService.activarMonopatin(idCuenta, idParada, idMonopatin);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Pausar un monopatín")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monopatín pausado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @PutMapping("/pausar")
    public ResponseEntity<Void> pausar(@RequestParam("idMonopatin") Long idMonopatin) {
        usuarioService.pausarMonopatin(idMonopatin);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Finalizar un viaje en monopatín")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Viaje finalizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @PutMapping("/finalizarViaje")
    public ResponseEntity<?> finalizarViaje(@RequestParam("idCuenta") Long idCuenta,
                                            @RequestParam("idMonopatin") Long idMonopatin) {
        usuarioService.finalizarViaje(idCuenta, idMonopatin);
        return ResponseEntity.ok().build();
    }

}
