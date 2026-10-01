package com.example.microservicio_administrador;

import com.example.microservicio_administrador.controller.AdministradorController;
import com.example.microservicio_administrador.dto.AdministradorDto;
import com.example.microservicio_administrador.feignClient.ViajeFeignClient;
import com.example.microservicio_administrador.model.ReporteTotalFacturadoEntreMesesDeAnio;
import com.example.microservicio_administrador.service.AdministradorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdministradorController.class)
public class AdministradorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdministradorService administradorService;

    @InjectMocks
    private AdministradorController administradorController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(administradorController).build();
    }

    @Test
    public void testGetAllAdministradores() throws Exception {
        when(administradorService.getAllAdministradores()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/administradores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetAdministradorById() throws Exception {
        AdministradorDto adminDto = new AdministradorDto();
        adminDto.setId(1L);
        adminDto.setNombre("Admin1");

        when(administradorService.getAdministradorById(1L)).thenReturn(adminDto);

        mockMvc.perform(get("/api/administradores/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Admin1"));
    }

    // --- TEST: Guardar con 201 Created y Header Location ---
    @Test
    public void testSaveAdministradorExitoso() throws Exception {
        AdministradorDto inputDto = new AdministradorDto();
        // Seteá acá los campos necesarios de tu DTO

        AdministradorDto savedDto = new AdministradorDto();
        savedDto.setId(10L); // Simulamos que la DB le asignó el ID 10

        when(administradorService.save(any(AdministradorDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                // 1. Verifica HTTP 201 Created
                .andExpect(status().isCreated())
                // 2. Verifica que la cabecera Location exista y termine en /10
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", endsWith("/api/administradores/10")))
                // 3. Verifica el cuerpo de la respuesta
                .andExpect(jsonPath("$.id").value(10L));
    }


    // --- TEST: Delete cuando el recurso NO existe (404) ---
    @Test
    public void testDeleteAdministradorNoEncontrado() throws Exception {
        Long idInexistente = 999L;
        // Simulamos que el service devuelve false porque no existía
        when(administradorService.delete(idInexistente)).thenReturn(false);

        mockMvc.perform(delete("/api/administradores/{id}", idInexistente))
                .andExpect(status().isNotFound()); // Verifica HTTP 404
    }

    // --- TEST: Delete cuando el recurso SÍ existe (204) ---
    @Test
    public void testDeleteAdministradorExitoso() throws Exception {
        Long idExistente = 1L;
        // Simulamos que el service eliminó el registro exitosamente
        when(administradorService.delete(idExistente)).thenReturn(true);

        mockMvc.perform(delete("/api/administradores/{id}", idExistente))
                .andExpect(status().isNoContent()); // Verifica HTTP 204 No Content
    }

    @Test
    public void testGetReporteTotalFacturadoEntreMesesDeAnio() throws Exception {
        ReporteTotalFacturadoEntreMesesDeAnio reporte = new ReporteTotalFacturadoEntreMesesDeAnio();


        when(administradorService.getReporteTotalFacturadoEntreMesesDeAnio(1L, 12L, 2023L)).thenReturn(reporte);

        mockMvc.perform(get("/api/administradores/reporteTotalFacturadoEntreMesesDeAnio")
                        .param("mesInicio", "1")
                        .param("mesFin", "12")
                        .param("anio", "2023")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}