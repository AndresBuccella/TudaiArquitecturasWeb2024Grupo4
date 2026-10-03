package com.example.microservicio_monopatin;

import com.example.microservicio_monopatin.controller.MonopatinController;
import com.example.microservicio_monopatin.dtos.MonopatinDTO;
import com.example.microservicio_monopatin.dtos.ReporteUsoDto;
import com.example.microservicio_monopatin.service.MonopatinService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonopatinController.class)
public class MonopatinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonopatinService monopatinService;

    @InjectMocks
    private MonopatinController monopatinController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(monopatinController).build();
    }

    @Test
    public void testGetAllMonopatines() throws Exception {
        when(monopatinService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/monopatines")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetMonopatinById() throws Exception {
        // Crea un objeto Monopatin simulado
        MonopatinDTO monopatin = new MonopatinDTO();
        monopatin.setId(1L);

        // Simula que el servicio devuelve el MonopatinDTO
        when(monopatinService.findById(1L)).thenReturn(monopatin);

        // Realiza la solicitud GET y verifica la respuesta
        mockMvc.perform(get("/api/monopatines/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testGetMonopatinByIdNotFound() throws Exception {
        when(monopatinService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/monopatines/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveMonopatin() throws Exception {
        MonopatinDTO monopatin = new MonopatinDTO();
        monopatin.setId(1L);


        when(monopatinService.save(any(MonopatinDTO.class))).thenReturn(monopatin);

        mockMvc.perform(post("/api/monopatines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kmRecorridosTotales\": 100, \"tiempoRecorridosTotales\": 120, \"disponible\": true, \"longitud\": 123.456, \"latitud\": 78.910, \"viajeActivo\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

    }

    @Test
    public void testDeleteMonopatin() throws Exception {
        // Crea un objeto Monopatin para simular el que se eliminará
        MonopatinDTO monopatin = new MonopatinDTO();
        monopatin.setId(1L);

        // Simula que la búsqueda devuelve el objeto Monopatin
        when(monopatinService.findById(1L)).thenReturn(monopatin);

        // Simula que el método delete no lanza excepciones
        doNothing().when(monopatinService).delete(monopatin.getId());

        // Realiza la solicitud DELETE y espera un estado 204 No Content
        mockMvc.perform(delete("/api/monopatines/1"))
                .andExpect(status().isNoContent());

        // Verifica que el método delete fue llamado exactamente una vez
        verify(monopatinService, times(1)).delete(monopatin.getId());
    }

    @Test
    public void testHabilitarMonopatin() throws Exception {
        when(monopatinService.habilitar(1L)).thenReturn(true);

        mockMvc.perform(put("/api/monopatines/habilitar/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeshabilitarMonopatin() throws Exception {
        when(monopatinService.deshabilitar(1L)).thenReturn(true);

        mockMvc.perform(put("/api/monopatines/deshabilitar/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetReporteUsoPorKilometro() throws Exception {
        List<ReporteUsoDto> reporte = List.of(new ReporteUsoDto(/* parámetros según tu DTO */));

        when(monopatinService.getReporteUsoMonopatinesPorKilometro()).thenReturn(reporte);

        mockMvc.perform(get("/api/monopatines/reportePorKilometros")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(reporte.size()));
    }

    @Test
    public void testGetReporteUsoCompleto() throws Exception {
        List<ReporteUsoDto> reporte = List.of(new ReporteUsoDto(/* parámetros según tu DTO */));

        when(monopatinService.getReporteUsoMonopatinesCompleto()).thenReturn(reporte);

        mockMvc.perform(get("/api/monopatines/reportePorTiempoTotal")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(reporte.size()));
    }

    @Test
    public void testGetReporteUsoCompletoSinPausa() throws Exception {
        List<ReporteUsoDto> reporte = List.of(new ReporteUsoDto(/* parámetros según tu DTO */));

        when(monopatinService.getReporteUsoMonopatinesCompletoSinPausa()).thenReturn(reporte);

        mockMvc.perform(get("/api/monopatines/reportePorTiempoSinPausa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(reporte.size()));
    }

    @Test
    public void testReservarMonopatin() throws Exception {
        MonopatinDTO monopatinDto = new MonopatinDTO();
        monopatinDto.setId(1L);

        when(monopatinService.iniciarViaje("1", 1L)).thenReturn(monopatinDto);

        mockMvc.perform(put("/api/monopatines/reservarMonopatin/parada/1/monopatin/1/reservar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testPausarMonopatin() throws Exception {
        doNothing().when(monopatinService).pausarMonopatin(1L);

        mockMvc.perform(put("/api/monopatines/pausa/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testFinalizarRecorrido() throws Exception {
        Long idMonopatin = 1L;
        String paradaId = "2";
        Long viajeId = 3L;
        Long kmRecorridos = 10L;

        // Simulación del método
        doAnswer(invocation -> null).when(monopatinService).pararMonopatin(idMonopatin, paradaId, viajeId, kmRecorridos);

        mockMvc.perform(put("/api/monopatines/1/finalizarRecorrido")
                        .param("paradaId", paradaId)
                        .param("viajeId", viajeId.toString())
                        .param("kmRecorridos", kmRecorridos.toString()))
                .andExpect(status().isOk());

        // Verificar que el método se llamó con los parámetros correctos
        verify(monopatinService, times(1)).pararMonopatin(idMonopatin, paradaId, viajeId, kmRecorridos);
    }
}
