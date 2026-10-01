package com.example.microservicio_administrador.service;

import com.example.microservicio_administrador.dto.AdministradorDto;
import com.example.microservicio_administrador.entity.Administrador;
import com.example.microservicio_administrador.feignClient.ViajeFeignClient;
import com.example.microservicio_administrador.model.ReporteTotalFacturadoEntreMesesDeAnio;
import com.example.microservicio_administrador.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;
    @Autowired
    private ViajeFeignClient viajeFeignClient;

    @Transactional(readOnly = true)
    public List<AdministradorDto> getAllAdministradores() {

        return administradorRepository.findAll()
                .stream()
                .map(AdministradorDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdministradorDto getAdministradorById(Long id) {

        return administradorRepository.findById(id)
                .map(AdministradorDto::new)
                .orElseThrow(() -> new RuntimeException("No se encontro el administrador"));
    }

    @Transactional
    public AdministradorDto save(AdministradorDto newAdmin) {
        Administrador admin = new Administrador();
        admin.setNombre(newAdmin.getNombre());
        administradorRepository.save(admin);
        return newAdmin;
    }

    @Transactional
    public boolean delete(Long id) {
        if (administradorRepository.existsById(id)) {
            administradorRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public ReporteTotalFacturadoEntreMesesDeAnio getReporteTotalFacturadoEntreMesesDeAnio(Long mesInicio, Long mesFin, Long anio) {
        return viajeFeignClient.getReporteTotalFacturadoEntreMesesDeAnio(mesInicio, mesFin, anio);
    }
}
