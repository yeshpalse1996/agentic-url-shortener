
package com.agenticengineering.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(
                                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth


                                        // Public Register
                                                .requestMatchers(
                                                                HttpMethod.POST, "/api/auth/register")
                                                .permitAll()

                                                // Public login
                                                .requestMatchers(
                                                                HttpMethod.POST, "/api/auth/login")
                                                .permitAll()

                                                // Public URL creation for now
                                                .requestMatchers(
                                                                HttpMethod.POST, "/api/urls")
                                                .authenticated()

                                                // Protect analytics and click history
                                                .requestMatchers(
                                                                "/api/urls/*/analytics/**",
                                                                "/api/urls/*/clicks")
                                                .authenticated()

                                                // Public redirect: GET /api/urls/{shortCode}
                                                .requestMatchers(
                                                                HttpMethod.GET, "/api/urls/*")
                                                .permitAll()

                                                // Protect everything else
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

                return http.build();
        }
}