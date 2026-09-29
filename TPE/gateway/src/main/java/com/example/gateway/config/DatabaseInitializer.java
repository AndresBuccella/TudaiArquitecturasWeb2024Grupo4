package com.example.gateway.config;

import com.example.gateway.entity.Authority;
import com.example.gateway.entity.User;
import com.example.gateway.repository.AuthorityRepository;
import com.example.gateway.repository.UserRepository;
import com.example.gateway.security.AuthorityConstant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DatabaseInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            AuthorityRepository authorityRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Crear roles si no existen
            if (authorityRepository.count() == 0) {
                Authority adminRole = new Authority(AuthorityConstant._ADMIN);
                Authority clienteRole = new Authority(AuthorityConstant._CLIENTE);
                Authority mantenimientoRole = new Authority(AuthorityConstant._MANTENIMIENTO);

                authorityRepository.save(adminRole);
                authorityRepository.save(clienteRole);
                authorityRepository.save(mantenimientoRole);
                System.out.println(">>> Roles base insertados en la base de datos");
            }

            // 2. Crear usuario Admin inicial para poder probar el sistema
            if (userRepository.findOneWithAuthoritiesByUsernameIgnoreCase("admin").isEmpty()) {
                Authority adminAuth = authorityRepository.findById(AuthorityConstant._ADMIN).orElseThrow();

                User admin = new User("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Password inicial
                admin.setAuthorities(Set.of(adminAuth));

                userRepository.save(admin);
                System.out.println(">>> Usuario 'admin' inicial creado con contraseña 'admin123'");
            }
        };
    }
}
