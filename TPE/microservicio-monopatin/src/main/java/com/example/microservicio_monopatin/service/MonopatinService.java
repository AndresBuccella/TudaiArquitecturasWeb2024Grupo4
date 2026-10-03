package com.example.microservicio_monopatin.service;

import com.example.microservicio_monopatin.dtos.MonopatinDTO;
import com.example.microservicio_monopatin.dtos.ReporteUsoDto;
import com.example.microservicio_monopatin.entity.Monopatin;
import com.example.microservicio_monopatin.feignClient.AdministradorFeignClient;
import com.example.microservicio_monopatin.feignClient.ParadaFeignClient;
import com.example.microservicio_monopatin.feignClient.ViajeFeignClient;
import com.example.microservicio_monopatin.models.Parada;
import com.example.microservicio_monopatin.repository.MonopatinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class MonopatinService {

    @Autowired
    MonopatinRepository monopatinRepository;

    @Autowired
    private AdministradorFeignClient administradorFeignClient;

    @Autowired
    private ParadaFeignClient paradaFeignClient;

    @Autowired
    private ViajeFeignClient viajeFeignClient;  // Cliente Feign para el microservicio de Viaje

    @Transactional(readOnly = true)
    public List<MonopatinDTO> getAll() {
        List<Monopatin> monopatines = monopatinRepository.findAll();
        return monopatines.stream()
                .map(MonopatinDTO::new)  // Convierte a MonopatinDTO
                .collect(Collectors.toList());
    }

    @Transactional
    public MonopatinDTO save(MonopatinDTO monopatinDto) {
        Monopatin monopatin = new Monopatin(monopatinDto);
        monopatinRepository.save(monopatin);
        return monopatinDto;
    }

    @Transactional
    public boolean delete(Long id) {
        if (monopatinRepository.existsById(id)){
            monopatinRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public MonopatinDTO findById(Long id) {
        Monopatin m = monopatinRepository.findById(id).orElse(null);
        return m != null ? new MonopatinDTO(m) : null;
    }
    public List<MonopatinDTO> findByIds(List<Long> id) {
        List<Monopatin> monopatins = monopatinRepository.findAllById(id);
        List<MonopatinDTO> listMonopatinsDTO = new ArrayList<>();
        for(Monopatin m : monopatins){
            MonopatinDTO monopatinDTO = new MonopatinDTO(m);
            listMonopatinsDTO.add(monopatinDTO);
        }
        return listMonopatinsDTO;
    }
    @Transactional
    public Monopatin update(Monopatin monopatin) {
        return monopatinRepository.save(monopatin);
    }


    // Método para pausar el monopatín
    @Transactional
    public void pausarMonopatin(Long monopatinId) {
        Monopatin monopatin = monopatinRepository.findById(monopatinId)
                .orElseThrow(() -> new RuntimeException("Monopatin no encontrado"));

        viajeFeignClient.registrarInicioPausa(monopatin.getIdViajeActivo(), LocalDateTime.now());  // Registrar pausa en Viaje
        monopatin.setDisponible(false);
        monopatinRepository.save(monopatin);
    }

    // Método para reiniciar el monopatín y calcular la tarifa de reinicio
    @Transactional
    public void reiniciarMonopatin(Long monopatinId) {
        Monopatin monopatin = monopatinRepository.findById(monopatinId)
                .orElseThrow(() -> new RuntimeException("Monopatin no encontrado"));

        LocalDateTime finPausa = LocalDateTime.now();
        LocalDateTime inicioPausa = viajeFeignClient.obtenerInicioUltimaPausa(monopatinId);
        long minutosPausa = Duration.between(inicioPausa, finPausa).toMinutes();

        double tarifa = calcularTarifaReinicio(minutosPausa);
        viajeFeignClient.registrarFinPausa(monopatinId, finPausa);  // Actualizar fin de pausa en Viaje

        administradorFeignClient.aplicarTarifaExtra(monopatinId, tarifa);
        monopatinRepository.save(monopatin);
    }

    // Método para calcular la tarifa de reinicio
    private double calcularTarifaReinicio(long minutosPausa) {
        double tarifaBase = administradorFeignClient.obtenerPrecioBase();
        if (minutosPausa > 15) {
            tarifaBase += administradorFeignClient.obtenerTarifaExtra();
        }
        return tarifaBase;
    }

    // Método para iniciar un viaje al retirar el monopatín de una parada
    @Transactional
    public MonopatinDTO iniciarViaje(String paradaId, Long monopatinId) {
        Monopatin monopatin = monopatinRepository.findById(monopatinId)
                .orElseThrow(() -> new RuntimeException("Monopatin no encontrado"));

        // Verificar que el monopatín esté disponible para iniciar el viaje
        if (monopatin.getDisponible()) {
            // Registrar el inicio del viaje en el microservicio de Viaje
            LocalDateTime fechaHoraInicio = LocalDateTime.now();
            viajeFeignClient.iniciarViaje(monopatinId, fechaHoraInicio);

            // Cambiar el estado del monopatín a no disponible
            monopatin.setDisponible(false);
            monopatinRepository.save(monopatin);

            paradaFeignClient.quitarMonopatin(paradaId, monopatinId);

            return new MonopatinDTO(monopatin);
        } else {
            throw new RuntimeException("El monopatín no está disponible para iniciar un viaje");
        }
    }

    // Método para ubicar monopatín en una parada válida y finalizar el viaje si está en uso
    @Transactional
    public boolean pararMonopatin(Long monopatinId, String paradaId, Long viajeId, Long kmRecorridos) {
        Monopatin monopatin = monopatinRepository.findById(monopatinId).orElse(null);
        if (monopatin != null && paradaId != null && !monopatin.getDisponible()) {
            Parada parada = paradaFeignClient.getParadaById(paradaId);
            if (parada != null && esParadaPermitida(monopatin, parada)) {

                // Finalizar el viaje llamando al microservicio Viaje
                finalizarViaje(monopatinId, viajeId, kmRecorridos);

                monopatin.setDisponible(true);  // Monopatín vuelve a estar disponible
                monopatinRepository.save(monopatin);

                //Se coloca el monopatin en la parada
                parada.getIdMonopatines().add(monopatin.getId());
                paradaFeignClient.save(parada);
                return true;
            }
        }
        return false;
    }

    // Método para verificar si el monopatín está en el rango de la parada
    private boolean esParadaPermitida(Monopatin monopatin, Parada parada) {
        double rangoPermitido = 0.0005; // Aproximadamente 50 m, ajustable
        return Math.abs(monopatin.getLatitud() - parada.getLatitud()) < rangoPermitido &&
                Math.abs(monopatin.getLongitud() - parada.getLongitud()) < rangoPermitido;
    }

    // Método para finalizar el viaje
    private void finalizarViaje(Long monopatinId, Long viajeId, Long kmRecorridos) {
        LocalDateTime fechaHoraFin = LocalDateTime.now();

        // Llamada al microservicio Viaje para registrar la finalización
        viajeFeignClient.finalizarViaje(viajeId, fechaHoraFin, kmRecorridos);
    }

    @Transactional
    public Boolean habilitar(Long monopatinId) {
        return monopatinRepository.habilitar(monopatinId) == 1;
    }

    @Transactional
    public Boolean deshabilitar(Long monopatinId) {
        return monopatinRepository.deshabilitar(monopatinId) == 1;
    }

    @Transactional(readOnly = true)
    public List<ReporteUsoDto> getReporteUsoMonopatinesCompleto() {
        try {
            List<ReporteUsoDto> reporte = monopatinRepository.reporteUsoCompleto();

            if (reporte == null || reporte.isEmpty())
                return Collections.emptyList();

            return reporte;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte de uso de monopatines completo", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ReporteUsoDto> getReporteUsoMonopatinesCompletoSinPausa() {
        try {
            ResponseEntity<Map<Long, Long>> response = viajeFeignClient.getPausasMonopatines();
            Map<Long, Long> pausasMonopatines = (response != null && response.getBody() != null) ? response.getBody() : Collections.emptyMap();

            List<ReporteUsoDto> reportes = this.getReporteUsoMonopatinesCompleto();

            if (pausasMonopatines.isEmpty()) {
                return reportes;
            }

            for (ReporteUsoDto reporte : reportes) {
                Long tiempoTotal = reporte.getTiempoTotal();
                if (tiempoTotal != null) {
                    Long pausa = pausasMonopatines.get(reporte.getIdMonopatin());
                    if (pausa != null) {
                        reporte.setTiempoTotal(tiempoTotal - pausa);  // Restar si el valor existe
                    }
                } else {
                    // En caso de que tiempoTotal sea null, asigna un valor por defecto
                    reporte.setTiempoTotal(0L);
                }
            }
            return reportes;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte de uso de monopatines completo sin pausa", e);
        }
    }


    /*
         @Transactional(readOnly = true)
        public List<ReporteUsoDto> getReporteUsoMonopatinesCompletoSinPausa(){
            try {
                Map<Long, Long> pausasMonopatines = (Map<Long, Long>) viajeFeignClient.getPausasMonopatines().getBody();
                List<ReporteUsoDto> reportes = this.getReporteUsoMonopatinesCompleto();

                if(pausasMonopatines == null || pausasMonopatines.isEmpty())
                    return reportes;

                for(ReporteUsoDto reporte : reportes){
                    reporte.setTiempoTotal(
                            reporte.getTiempoTotal()
                                    -
                                    pausasMonopatines.get(reporte.getIdMonopatin()));
                }
                return reportes;
            } catch (Exception e) {
                throw new RuntimeException("Error al generar el reporte de uso de monopatines completo sin pausa", e);
            }
        }
    */
    @Transactional(readOnly = true)
    public List<ReporteUsoDto> getReporteUsoMonopatinesPorKilometro() {
        try {
            List<ReporteUsoDto> reporte = monopatinRepository.reporteUsoPorKilometro();

            if (reporte == null || reporte.isEmpty())
                return Collections.emptyList();

            return reporte;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte de uso de monopatines por kilometros", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ReporteUsoDto> getReporteMonopatinesPorTiempoConPausas() {

        try {
            List<ReporteUsoDto> reportes = monopatinRepository.reporteUsoPorTiempo();
            if (reportes == null || reportes.isEmpty())
                return Collections.emptyList();

            return reportes;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte de uso de monopatines por tiempo con pausas", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ReporteUsoDto> getReporteMonopatinesPorTiempoSinPausas() {

        try {
            Map<Long, Long> pausasMonopatines = viajeFeignClient.getPausasMonopatines().getBody();
            List<ReporteUsoDto> reportes = monopatinRepository.reporteUsoPorTiempo();
            if (reportes == null || reportes.isEmpty())
                return Collections.emptyList();

            if (pausasMonopatines == null || pausasMonopatines.isEmpty())
                return reportes;

            for (ReporteUsoDto reporte : reportes) {
                reporte.setTiempoTotal(
                        reporte.getTiempoTotal()
                                -
                                pausasMonopatines.get(reporte.getIdMonopatin()));
            }

            return reportes;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte de uso de monopatines por tiempo sin pausas", e);
        }
    }
}
