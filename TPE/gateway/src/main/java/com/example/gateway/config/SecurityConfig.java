package com.example.gateway.config;

import com.example.gateway.security.AuthorityConstant;
import com.example.gateway.security.jwt.JwtFilter;
import com.example.gateway.security.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    public SecurityConfig( TokenProvider tokenProvider ) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http ) throws Exception {
        http
                .csrf( AbstractHttpConfigurer::disable );
        http
                .sessionManagement( s -> s.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) );
        http
                .securityMatcher("/api/**" )
                .authorizeHttpRequests( authz -> authz
                        //el orden va de más específica a menos específica
                        .requestMatchers( HttpMethod.POST, "/api/authenticate").permitAll()
                        .requestMatchers( HttpMethod.POST, "/api/users/sign-in").permitAll() // es usuarios pero del gateway(para autenticacion)
                        //Administrador
                        .requestMatchers(HttpMethod.GET, "/api/administradores").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/administradores/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.PUT, "/api/administradores/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/administradores/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.DELETE, "/api/administradores/**").hasAuthority(AuthorityConstant._ADMIN)

                        //Cuenta
                        .requestMatchers(HttpMethod.GET, "/api/cuentas").hasAuthority(AuthorityConstant._ADMIN) //Lista de cuentas
                        .requestMatchers(HttpMethod.GET, "/api/cuentas/habilitadas").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/cuentas/deshabilitadas").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers( HttpMethod.GET, "/api/cuentas/**").permitAll() //cada uno puede acceder a su cuenta

                        .requestMatchers(HttpMethod.PUT, "/api/cuentas/cobrarViaje").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/cuentas/habilitar/**").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/cuentas/deshabilitar/**").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/cuentas/**").permitAll() //editar cuenta

                        .requestMatchers(HttpMethod.POST, "/api/cuentas").permitAll()//save(crear cuenta)

                        .requestMatchers(HttpMethod.DELETE, "/api/cuentas/**").hasAuthority(AuthorityConstant._ADMIN)

                        //Mantenimiento
                        .requestMatchers(HttpMethod.GET, "/api/mantenimientos").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)
                        .requestMatchers(HttpMethod.GET, "/api/mantenimientos/**").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)

                        .requestMatchers(HttpMethod.PUT, "/api/mantenimientos/**").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)

                        .requestMatchers(HttpMethod.POST, "/api/mantenimientos/**").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)

                        .requestMatchers(HttpMethod.DELETE, "/api/mantenimientos/**").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)

                        //Monopatin
                        .requestMatchers(HttpMethod.GET, "/api/monopatines").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/monopatines/reportePorTiempoTotal").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)
                        .requestMatchers(HttpMethod.GET, "/api/monopatines/reportePorTiempoSinPausa").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)
                        .requestMatchers(HttpMethod.GET, "/api/monopatines/**").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)

                        .requestMatchers(HttpMethod.PUT, "/api/monopatines/reservarMonopatin/parada/**/monopatin/**/reservar").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/monopatines/**/finalizarRecorrido").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/monopatines/habilitar").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)
                        .requestMatchers(HttpMethod.PUT, "/api/monopatines/deshabilitar").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._MANTENIMIENTO)
                        .requestMatchers(HttpMethod.PUT, "/api/monopatines/pausa/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/monopatines/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.DELETE, "/api/monopatines/**").hasAuthority(AuthorityConstant._ADMIN)

                        //Parada
                        .requestMatchers(HttpMethod.GET, "/api/paradas").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/paradas/habilitadas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paradas/deshabilitadas").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/paradas/cercanos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paradas/**").permitAll()

                        .requestMatchers(HttpMethod.PUT, "/api/paradas/**/monopatin/**/quitarMonopatin").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/paradas/habilitar/**").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/paradas/deshabilitar/**").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/paradas/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/paradas").hasAuthority(AuthorityConstant._ADMIN) //save

                        .requestMatchers(HttpMethod.DELETE, "/api/paradas/**").hasAuthority(AuthorityConstant._ADMIN)

                        //Usuario
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/habilitados").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/deshabilitados").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/getByUsername/**").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAnyAuthority(AuthorityConstant._ADMIN, AuthorityConstant._CLIENTE)

                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/reservarMonopatin").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/pausar").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/finalizarViaje").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/habilitar/**").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/deshabilitar/**").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll() //save

                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAuthority(AuthorityConstant._ADMIN)

                        //Viaje
                        .requestMatchers(HttpMethod.GET, "/api/viajes").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/viajes/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.PUT, "/api/viajes/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/viajes").hasAuthority(AuthorityConstant._ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/viajes/**").hasAuthority(AuthorityConstant._ADMIN)

                        .requestMatchers(HttpMethod.DELETE, "/api/viajes/**").hasAuthority(AuthorityConstant._ADMIN)

                        .anyRequest().authenticated()
                )
                .httpBasic( Customizer.withDefaults() )
                .addFilterBefore( new JwtFilter( this.tokenProvider ), UsernamePasswordAuthenticationFilter.class );
        return http.build();
    }

}
