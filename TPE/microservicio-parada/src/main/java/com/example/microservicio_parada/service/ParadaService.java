package com.example.microservicio_parada.service;

import com.example.microservicio_parada.entity.Parada;
import com.example.microservicio_parada.feignClients.MonopatinFeignClient;
import com.example.microservicio_parada.models.Monopatin;
import com.example.microservicio_parada.repository.ParadaRepository;
import com.example.microservicio_parada.repository.ParadaRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ParadaService {

    @Autowired
    private ParadaRepository paradaRepository;

    @Autowired
    private ParadaRepositoryCustom paradaRepositoryCustom;

    @Autowired
    private MonopatinFeignClient monopatinFeignClient;

    // Create
    public Parada save(Parada parada) {
        return paradaRepository.save(parada);
    }

    // Read
    public List<Parada> getAll() {
        return paradaRepository.findAll();
    }

    public List<Parada> getAllHabilitadas() {
        return paradaRepository.findByHabilitadoTrue();
    }

    public List<Parada> getAllDeshabilitadas() {
        return paradaRepository.findByHabilitadoFalse();
    }

    public Parada findById(String id) {
        return paradaRepository.findById(id).orElse(null);
    }

    // Update
    public Parada update(Parada parada) {
        return paradaRepository.save(parada);
    }

    // Delete
    public void delete(String id) {
        paradaRepository.deleteById(id);
    }

    // Habilitar Parada
    public void habilitar(String id) {
        paradaRepositoryCustom.habilitar(id);
    }

    // Deshabilitar Parada
    public void deshabilitar(String id) {
        paradaRepositoryCustom.deshabilitar(id);
    }

    // Obtener Monopatines por ID de Parada
    public List<Monopatin> getMonopatinesById(String id) {
        List<Monopatin> salida = new ArrayList<>();
        Optional<Parada> paradaOptional = paradaRepository.findById(id);

        if (paradaOptional.isPresent()) {
            Parada parada = paradaOptional.get();
            if (parada.getIdMonopatines() != null) {
                salida.add(monopatinFeignClient.getMonopatinByIds(parada.getIdMonopatines()));
            }
        }
        return salida;
    }

    // Obtener Monopatines Cercanos
    public List<Monopatin> getMonopatinesCercanos(double latitud, double longitud, double radio) {
        List<Parada> paradas = getAllHabilitadas();
        List<Parada> paradasCercanas = paradas.stream()
                .filter(parada -> calcularDistancia(latitud, longitud, parada.getLatitud(), parada.getLongitud()) <= radio)
                .toList();

        List<Monopatin> monopatinesCercanos = new ArrayList<>();
        for (Parada parada : paradasCercanas) {
            if (parada.getIdMonopatines() != null) {
                for (Long idMonopatin : parada.getIdMonopatines()) {
                    monopatinesCercanos.add(monopatinFeignClient.getMonopatinById(idMonopatin));
                }
            }
        }
        return monopatinesCercanos;
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA = 6371; // Radio de la Tierra en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA * c;
    }

    public Parada quitarMonopatin(String idParada, Long idMonopatin) {
        try {
            Parada parada = findById(idParada);
            if (parada != null) {
                parada.quitarMonopatin(idMonopatin);
                return paradaRepository.save(parada);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
