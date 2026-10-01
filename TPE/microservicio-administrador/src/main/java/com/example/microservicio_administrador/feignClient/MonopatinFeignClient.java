package com.example.microservicio_administrador.feignClient;

import com.example.microservicio_administrador.model.Monopatin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "microservicio-monopatin")
public interface MonopatinFeignClient {

    @GetMapping("")
    List<Monopatin> getAll();

    @PostMapping("")
    Monopatin save(@RequestBody Monopatin monopatin);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") Long id);

    @PutMapping("/deshabilitar/{id}")
    boolean deshabilitarMonopatin(@PathVariable("id") Long id);

    @PutMapping("/habilitar/{id}")
    boolean habilitarMonopatin(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    Monopatin updateMonopatin(@PathVariable("id") Long id, @RequestBody Monopatin Monopatin);
}
