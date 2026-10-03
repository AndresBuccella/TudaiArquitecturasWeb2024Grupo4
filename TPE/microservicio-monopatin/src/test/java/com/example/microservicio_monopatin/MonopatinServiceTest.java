package com.example.microservicio_monopatin;

import com.example.microservicio_monopatin.dtos.MonopatinDTO;
import com.example.microservicio_monopatin.dtos.ReporteUsoDto;
import com.example.microservicio_monopatin.entity.Monopatin;
import com.example.microservicio_monopatin.feignClient.ParadaFeignClient;
import com.example.microservicio_monopatin.feignClient.ViajeFeignClient;
import com.example.microservicio_monopatin.models.Parada;
import com.example.microservicio_monopatin.repository.MonopatinRepository;
import com.example.microservicio_monopatin.service.MonopatinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MonopatinServiceTest {

    @Mock
    private MonopatinRepository monopatinRepository;

    @InjectMocks
    private MonopatinService monopatinService;

    @Mock
    private ViajeFeignClient viajeFeignClient;
    @Mock
    private ParadaFeignClient paradaFeignClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveMonopatin() {
        MonopatinDTO monopatinDTO = new MonopatinDTO();
        // Configura otros campos necesarios
        Monopatin monopatin = new Monopatin();
        monopatinDTO.setId(1L);
        // Configura otros campos necesarios

        when(monopatinRepository.save(any(Monopatin.class))).thenReturn(monopatin);

        MonopatinDTO result = monopatinService.save(monopatinDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // Verifica otros campos si es necesario
    }

    @Test
    public void testGetAllMonopatines() {
        when(monopatinRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(monopatinService.getAll().isEmpty());
    }

    @Test
    public void testFindById() {
        Monopatin monopatin = new Monopatin();
        monopatin.setId(1L);
        // Configura otros campos necesarios

        when(monopatinRepository.findById(1L)).thenReturn(Optional.of(monopatin));

        Monopatin result = monopatinService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // Verifica otros campos si es necesario
    }

    @Test
    public void testDeleteMonopatin() {
        // Crea un objeto Monopatin simulado
        Monopatin monopatin = new Monopatin();
        monopatin.setId(1L);
        // Configura otros campos necesarios si es necesario

        // Simula que el repositorio encuentra el Monopatin
        when(monopatinRepository.findById(1L)).thenReturn(Optional.of(monopatin));

        doNothing().when(monopatinRepository).delete(monopatin);

        monopatinService.delete(monopatin);

        // Verifica que el método delete fue llamado una vez con el Monopatin correcto
        verify(monopatinRepository, times(1)).delete(monopatin);
    }

    @Test
    public void testHabilitarMonopatin() {
        Long monopatinId = 1L;

        // Simula que el método habilitar devuelve 1 (una fila afectada).
        when(monopatinRepository.habilitar(monopatinId)).thenReturn(1);

        // Llama al método habilitar del servicio.
        boolean result = monopatinService.habilitar(monopatinId);

        // Verifica el resultado.
        assertTrue(result);

        // Verifica que el método habilitar fue llamado una vez con el ID correcto.
        verify(monopatinRepository, times(1)).habilitar(monopatinId);
    }

    @Test
    public void testDeshabilitarMonopatin() {
        Long monopatinId = 1L;

        // Simula que el método deshabilitar devuelve 1 (una fila afectada).
        when(monopatinRepository.deshabilitar(monopatinId)).thenReturn(1);

        // Llama al método deshabilitar del servicio.
        boolean result = monopatinService.deshabilitar(monopatinId);

        // Verifica el resultado.
        assertTrue(result);

        // Verifica que el método deshabilitar fue llamado una vez con el ID correcto.
        verify(monopatinRepository, times(1)).deshabilitar(monopatinId);
    }

    @Test
    public void testIniciarViaje() {
        Monopatin monopatin = new Monopatin();
        monopatin.setId(1L);
        monopatin.setDisponible(true);

        // Crear una Parada simulada
        Parada paradaSimulada = new Parada();
        paradaSimulada.setId(1L);
        paradaSimulada.setNombre("Parada Central");

        // Mockear repositorio y feign clients
        when(monopatinRepository.findById(1L)).thenReturn(Optional.of(monopatin));
        when(monopatinRepository.save(any(Monopatin.class))).thenReturn(monopatin);
        when(paradaFeignClient.quitarMonopatin(eq("1"), eq(1L))).thenReturn(paradaSimulada);

        // Mock de viajeFeignClient
        doNothing().when(viajeFeignClient).iniciarViaje(eq(1L), any(LocalDateTime.class));

        MonopatinDTO result = monopatinService.iniciarViaje("1", 1L);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1L, result.getId());

    }

    @Test
    public void testPausarMonopatin() {
        Monopatin monopatin = new Monopatin();
        monopatin.setId(1L);

        when(monopatinRepository.findById(1L)).thenReturn(Optional.of(monopatin));

        monopatinService.pausarMonopatin(1L);

        verify(monopatinRepository, times(1)).save(monopatin);
    }

    @Test
    public void testPararMonopatin() {
        // Crear y configurar el monopatín
        Monopatin monopatin = new Monopatin();
        monopatin.setId(1L);
        monopatin.setLatitud(1050L); // Asignar un valor no nulo
        monopatin.setLongitud(2050L); // Asignar un valor no nulo
        monopatin.setDisponible(false);
        monopatin.setIdViajeActivo(1L);

        // Crear y configurar la parada
        Parada parada = new Parada();
        parada.setId(1L);
        parada.setLatitud(1050L);
        parada.setLongitud(2050L);
        parada.setIdMonopatines(new ArrayList<>());

        // Mockear el repositorio y FeignClient
        when(monopatinRepository.findById(1L)).thenReturn(Optional.of(monopatin));
        when(paradaFeignClient.getParadaById("1")).thenReturn(parada);

        // Ejecutar el método
        boolean resultado = monopatinService.pararMonopatin(1L, "1", 1L, 10L);

        // Validar el resultado
        assertTrue(resultado);
        verify(monopatinRepository, times(1)).save(monopatin);
        verify(paradaFeignClient, times(1)).save(parada);
    }


    @Test
    public void testGetReporteUsoMonopatinesPorKilometro() {
        // Configura un resultado esperado
        List<ReporteUsoDto> expectedReport = List.of(new ReporteUsoDto(/* parámetros según tu DTO */));

        when(monopatinService.getReporteUsoMonopatinesPorKilometro()).thenReturn(expectedReport);

        List<ReporteUsoDto> result = monopatinService.getReporteUsoMonopatinesPorKilometro();

        assertNotNull(result);
        assertEquals(expectedReport.size(), result.size());
    }

    @Test
    public void testGetReporteUsoMonopatinesCompleto() {
        // Configura un resultado esperado
        List<ReporteUsoDto> expectedReport = List.of(new ReporteUsoDto(/* parámetros según tu DTO */));

        when(monopatinService.getReporteUsoMonopatinesCompleto()).thenReturn(expectedReport);

        List<ReporteUsoDto> result = monopatinService.getReporteUsoMonopatinesCompleto();

        assertNotNull(result);
        assertEquals(expectedReport.size(), result.size());
    }

    @Test
    public void testGetReporteUsoMonopatinesCompletoSinPausa() {
        // Crear el reporte esperado
        List<ReporteUsoDto> expectedReport = List.of(new ReporteUsoDto(/* parámetros */));

        // Simular la respuesta del FeignClient con un mapa de pausas
        Map<Long, Long> pausasMonopatinesMock = new HashMap<>();
        pausasMonopatinesMock.put(1L, 5L);  // Por ejemplo, monopatín 1 tiene 5 segundos de pausa

        when(viajeFeignClient.getPausasMonopatines()).thenReturn(ResponseEntity.ok(pausasMonopatinesMock));
        when(monopatinService.getReporteUsoMonopatinesCompleto()).thenReturn(expectedReport);

        // Ejecutar el método
        List<ReporteUsoDto> result = monopatinService.getReporteUsoMonopatinesCompletoSinPausa();

        // Verificar que el resultado no sea nulo y que los tamaños coincidan
        assertNotNull(result);
        assertEquals(expectedReport.size(), result.size());
    }
}
