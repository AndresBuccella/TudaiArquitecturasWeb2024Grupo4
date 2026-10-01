package com.example.microservicio_administrador;

import com.example.microservicio_administrador.controller.MonopatinController;
import com.example.microservicio_administrador.feignClient.MonopatinFeignClient;
import com.example.microservicio_administrador.model.Monopatin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonopatinController.class)
public class MonopatinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonopatinFeignClient monopatinFeignClient;

    @Test
    public void testConsultarEnOperacion() throws Exception {
        Monopatin monopatin1 = new Monopatin();
        monopatin1.setDisponible(true);

        Monopatin monopatin2 = new Monopatin();
        monopatin2.setDisponible(false);
        monopatin2.setViajeActivo(null);

        List<Monopatin> monopatines = Arrays.asList(monopatin1, monopatin2);

        when(monopatinFeignClient.getAll()).thenReturn(monopatines);

        mockMvc.perform(get("/api/administradores/monopatines/enOperacion"))
                .andExpect(status().isOk())
                .andExpect(content().string("Monopatines en operación: 1, Monopatines en mantenimiento: 1"));
    }

    @Test
    public void testConsultarEnOperacionNoMonopatines() throws Exception {
        when(monopatinFeignClient.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/administradores/monopatines/enOperacion"))
                .andExpect(status().isOk())
                .andExpect(content().string("No hay monopatines disponibles."));
    }


    @Test
    public void testAddMonopatin() throws Exception {
        Monopatin newMonopatin = new Monopatin();
        newMonopatin.setId(1L);

        when(monopatinFeignClient.save(any(Monopatin.class))).thenReturn(newMonopatin);

        mockMvc.perform(post("/api/administradores/monopatines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testHabilitarMonopatin() throws Exception {
        when(monopatinFeignClient.habilitarMonopatin(1L)).thenReturn(true);

        mockMvc.perform(put("/api/administradores/monopatines/habilitar/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeshabilitarMonopatin() throws Exception {
        when(monopatinFeignClient.deshabilitarMonopatin(1L)).thenReturn(true);

        mockMvc.perform(put("/api/administradores/monopatines/deshabilitar/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateMonopatin() throws Exception {
        Monopatin updatedMonopatin = new Monopatin();
        updatedMonopatin.setId(1L);

        when(monopatinFeignClient.updateMonopatin(1L, updatedMonopatin)).thenReturn(updatedMonopatin);

        mockMvc.perform(put("/api/administradores/monopatines/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
    
}