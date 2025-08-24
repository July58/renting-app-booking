package org.example.bookingrent.config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final AuthFilter daprAuthFilter;

    public SecurityConfig(AuthFilter daprAuthFilter) {
        this.daprAuthFilter = daprAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/dapr/subscribe").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(daprAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

