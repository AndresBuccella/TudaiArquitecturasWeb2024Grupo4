package com.example.microservicio_administrador.service;

import com.example.microservicio_administrador.dto.TarifaDto;
import com.example.microservicio_administrador.entity.Tarifa;
import com.example.microservicio_administrador.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TarifaService {

    @Autowired
    TarifaRepository tarifaRepository;

    /*
    3f. Como administrador quiero hacer un ajuste de precios, y que a partir de cierta fecha
    el sistema habilite los nuevos precios.
    */
    @Transactional(readOnly = true)
    public TarifaDto getTarifaVigenteByTipo(String tipo) {
        Tarifa t = tarifaRepository.getTarifaVigente(tipo)
                .orElseThrow(() -> new RuntimeException("Tipo de tarifa no encontrada o no vigente:" + tipo));
        return new TarifaDto(t);
//        En memoria ram del servidor
//        List<Tarifa> tarifas = tarifaRepository.findFirstByTipoTarifaAndFechaInicioLessThanEqualOrderByFechaInicioDesc(tipo)
//                .stream()
//                .sorted(Comparator.comparing(Tarifa::getFechaInicio).reversed()) // Ordenar por fechaInicio
//                .toList();
//        //Si se necesita hacer en la bbdd se agrega en TarifaRespository List<Tarifa> findByTipoTarifaOrderByFechaInicioDesc(String tipoTarifa);
//
//        if (tarifas.isEmpty()) {
//            throw new RuntimeException("No se encontró el tipo de tarifa: " + tipo);
//        }
//
//        // Verificar si la fecha de inicio ha pasado
//        LocalDate hoy = LocalDate.now();
//        for (Tarifa t : tarifas) {
//            if (t.getFechaInicio() != null && !t.getFechaInicio().isAfter(hoy)) {
//                return new TarifaDto(t);  // El nuevo precio ya es válido
//            }
//        }
//        throw new RuntimeException("No hay tarifa vigente a la fecha para el tipo: " + tipo);
    }

    @Transactional(readOnly = true)
    public List<TarifaDto> getTarifas() {
        LocalDate hoy = LocalDate.now();

        return tarifaRepository.getAllTarifasOrdenadasPorFecha().stream()
                .map(tarifa -> {
                    TarifaDto dto = new TarifaDto(tarifa);
                    // Verificar si se aplica el nuevo precio según la fecha
                    if (tarifa.getFechaInicio() != null && !hoy.isBefore(tarifa.getFechaInicio())) {
                        dto.setPrecioTarifa(tarifa.getPrecioTarifa());
                    }
                    return dto;
                })
                .toList();
    }

    @Transactional
    public TarifaDto save(TarifaDto tarifaDto) {
        try {
            Tarifa tarifaModificada = new Tarifa(tarifaDto.getNombreTarifa(), tarifaDto.getTipoTarifa(), tarifaDto.getPrecioTarifa(), tarifaDto.getDescuentoTarifa(), tarifaDto.getFechaInicio());

            tarifaRepository.save(tarifaModificada);
            return tarifaDto;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar tarifa!" + e.getMessage());
        }
    }

    @Transactional
    public TarifaDto update(Long id, TarifaDto tarifaDto) {
        try {
            Tarifa tarifaExistente = tarifaRepository.findById(id).orElseThrow(() -> new RuntimeException("No existe un tarifa con id=" + id + "!"));

            tarifaExistente.setNombreTarifa(tarifaDto.getNombreTarifa());
            tarifaExistente.setTipoTarifa(tarifaDto.getTipoTarifa());
            tarifaExistente.setPrecioTarifa(tarifaDto.getPrecioTarifa());
            tarifaExistente.setDescuentoTarifa(tarifaDto.getDescuentoTarifa());
            tarifaExistente.setFechaInicio(tarifaDto.getFechaInicio());
            tarifaRepository.save(tarifaExistente);

            return new TarifaDto(tarifaExistente);

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar tarifa con id=" + id + "!" + e.getMessage());
        }
    }

    @Transactional
    public boolean delete(Long id) {
        try{
            tarifaRepository.deleteById(id);
            return true;
        }catch (Exception e){
            throw new RuntimeException("Error al eliminar tarifa con id=" + id + "!" + e.getMessage());
        }
    }
}
