package com.ticketapp.config;

import com.ticketapp.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@EnableMethodSecurity(prePostEnabled = true) // method-level security
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwt) throws Exception {

        http
                // CSRF'yi kapat (Swagger'dan POST yapacağız)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Bu URL'ler serbest
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/h2-console/**"
                        ).permitAll()
                        // sadece giriş yapmış kullanıcı:
                        .requestMatchers("/api/tickets/**").authenticated() // Bilet işlemleri: login zorunlu
                        // etkinlik oluşturmayı sadece ADMIN’e bırakmak istersen:
                        // Etkinlik CRUD -> sadece ADMIN
                        .requestMatchers(HttpMethod.POST,   "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        // Event listesini herkes görebilsin (istersen authenticated yapabilirsin)
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/reports/**").hasRole("ADMIN")
                          // Tickets: login şart
                                .requestMatchers("/api/tickets/**").authenticated()
                        .anyRequest().permitAll()


                )
                .exceptionHandling(e -> e
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint())
                )




        // H2 console için frame header'larını kapat
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        //* Login ekranlarını kapatalım (istemiyoruz)
        //*  .httpBasic(httpBasic -> httpBasic.disable())
        //*    .formLogin(form -> form.disable());



        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // com.ticketapp.config.SecurityConfig içinde bean olarak:
    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("""
            {"error":"forbidden","message":"You don't have permission to perform this action"}
        """);
        };
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("""
            {"error":"unauthorized","message":"Authentication required"}
        """);
        };
    }

}
