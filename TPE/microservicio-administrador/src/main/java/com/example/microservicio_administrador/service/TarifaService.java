package com.example.microservicio_administrador.service;

import com.example.microservicio_administrador.dto.TarifaDto;
import com.example.microservicio_administrador.entity.Tarifa;
import com.example.microservicio_administrador.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarifaService {

    @Autowired
    TarifaRepository tarifaRepository;

    /*
    3f. Como administrador quiero hacer un ajuste de precios, y que a partir de cierta fecha
    el sistema habilite los nuevos precios.
    */
    @Transactional(readOnly = true)
    public TarifaDto getTarifaByTipo(String tipo) {
        List<Tarifa> tarifas = tarifaRepository.getTarifaByTipo(tipo)
                .stream()
                .sorted((t1, t2) -> t2.getFechaInicio().compareTo(t1.getFechaInicio())) // Ordenar por fechaInicio
                .collect(Collectors.toList());

        if (tarifas.isEmpty()) {
            throw new RuntimeException("No se encontró el tipo de tarifa: " + tipo);
        }

        Tarifa tarifa = tarifas.get(0); // Seleccionar la más reciente

        // Verificar si la fecha de inicio ha pasado
        LocalDate hoy = LocalDate.now();
        if (tarifa.getFechaInicio() != null && !hoy.isBefore(tarifa.getFechaInicio())) {
            return new TarifaDto(tarifa);  // El nuevo precio ya es válido
        }

        // Si la fecha de inicio no ha pasado, retornar el precio actual
        TarifaDto tarifaDto = new TarifaDto(tarifa);
        tarifaDto.setPrecioTarifa(tarifa.getPrecioTarifa()); // Usa el precio actual
        return tarifaDto;
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
            throw new RuntimeException("Error al guardar carrera!" + e.getMessage());
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
