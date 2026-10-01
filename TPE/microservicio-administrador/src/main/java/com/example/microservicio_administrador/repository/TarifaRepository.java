package com.example.microservicio_administrador.repository;

import com.example.microservicio_administrador.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

//    @Query("SELECT t FROM Tarifa t WHERE t.tipoTarifa ILIKE :tipo")
//    Optional<Tarifa> getTarifaByTipo(String tipo);

    @Query("SELECT t " +
            "FROM Tarifa t " +
            "WHERE t.tipoTarifa ILIKE :tipo AND t.fechaInicio <= :fecha " +
            "ORDER BY t.fechaInicio DESC " +
            "LIMIT 1")
    Optional<Tarifa> getTarifaVigente(String tipo, LocalDate fecha);

    @Query("SELECT t FROM Tarifa t ORDER BY t.fechaInicio DESC")
    List<Tarifa> getAllTarifasOrdenadasPorFecha();
}
