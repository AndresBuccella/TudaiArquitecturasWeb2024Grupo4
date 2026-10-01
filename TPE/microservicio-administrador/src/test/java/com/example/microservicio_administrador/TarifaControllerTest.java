package com.example.microservicio_administrador;

import com.example.microservicio_administrador.controller.TarifaController;
import com.example.microservicio_administrador.dto.TarifaDto;
import com.example.microservicio_administrador.service.TarifaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TarifaController.class)
public class TarifaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarifaService tarifaService;


    @Test
    public void testAddTarifa() throws Exception {
        TarifaDto newTarifa = new TarifaDto();
        newTarifa.setId(1L);
        newTarifa.setNombreTarifa("Tarifa Básica");
        newTarifa.setTipoTarifa("Estándar");
        newTarifa.setPrecioTarifa(100.0);
        newTarifa.setDescuentoTarifa(10.0);
        newTarifa.setFechaInicio(LocalDate.of(2023, 1, 1));

        when(tarifaService.save(newTarifa)).thenReturn(newTarifa);

        mockMvc.perform(post("/api/administradores/tarifas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1," +
                                "\"nombreTarifa\":\"Tarifa Básica\"," +
                                "\"tipoTarifa\":\"Estándar\"," +
                                "\"precioTarifa\":100.0," +
                                "\"descuentoTarifa\":10.0," +
                                "\"fechaInicio\":\"2023-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreTarifa").value("Tarifa Básica"))
                .andExpect(jsonPath("$.tipoTarifa").value("Estándar"))
                .andExpect(jsonPath("$.precioTarifa").value(100.0))
                .andExpect(jsonPath("$.descuentoTarifa").value(10.0))
                .andExpect(jsonPath("$.fechaInicio").value("2023-01-01"));
    }

    @Test
    public void testUpdateTarifa() throws Exception {
        TarifaDto updatedTarifa = new TarifaDto();
        updatedTarifa.setId(1L);

        when(tarifaService.update(1L, updatedTarifa)).thenReturn(updatedTarifa);

        mockMvc.perform(put("/api/administradores/tarifas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testGetTarifaByTipo() throws Exception {
        TarifaDto tarifa = new TarifaDto();
        tarifa.setId(1L);
        tarifa.setTipoTarifa("Estándar");

        when(tarifaService.getTarifaVigenteByTipo("Estándar")).thenReturn(tarifa);

        mockMvc.perform(get("/api/administradores/tarifas/tipo/Estándar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipoTarifa").value("Estándar"));
    }

    // Prueba para el método delete
    @Test
    public void testDeleteTarifa() throws Exception {
        when(tarifaService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/administradores/tarifas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteTarifaNotFound() throws Exception {
        when(tarifaService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/administradores/tarifas/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":\"Tarifa no encontrada.\"}"));
    }

    // Prueba para el método getTarifasNormales
    @Test
    public void testGetTarifasNormales() throws Exception {
        TarifaDto tarifa1 = new TarifaDto();
        tarifa1.setId(1L);
        tarifa1.setTipoTarifa("Normal");

        TarifaDto tarifa2 = new TarifaDto();
        tarifa2.setId(2L);
        tarifa2.setTipoTarifa("Normal");

        List<TarifaDto> tarifas = Arrays.asList(tarifa1, tarifa2);

        when(tarifaService.getTarifas()).thenReturn(tarifas);

        mockMvc.perform(get("/api/administradores/tarifas/normales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].tipoTarifa").value("Normal"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].tipoTarifa").value("Normal"));
    }
}