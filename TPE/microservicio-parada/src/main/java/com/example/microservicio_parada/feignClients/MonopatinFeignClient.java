package com.example.microservicio_parada.feignClients;

import com.example.microservicio_parada.models.Monopatin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name="microservicio-monopatin")
public interface MonopatinFeignClient {
    @GetMapping("/{id}")
    Monopatin getMonopatinById(@PathVariable("id") Long id);

    @GetMapping("/{ids}")
    Monopatin getMonopatinByIds(@PathVariable("ids") List<Long> id);

    @PostMapping ("")
    Monopatin save(@RequestBody Monopatin monopatin);
}
