package com.tp.persistencia.persistencia_poliglota.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // Solo una sesión por usuario
                .maxSessionsPreventsLogin(false) // Invalida la sesión anterior
            )
            .authorizeHttpRequests(auth -> auth
                // ========== PÁGINAS WEB MVC ==========
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                .requestMatchers("/app/**").authenticated()
                .requestMatchers("/sensores", "/sensores/**").authenticated()
                .requestMatchers("/mediciones", "/mediciones/**").authenticated()
                .requestMatchers("/alertas", "/alertas/**").authenticated()
                .requestMatchers("/facturas", "/facturas/**").authenticated()
                .requestMatchers("/pagos", "/pagos/**").authenticated()
                .requestMatchers("/cuentas", "/cuentas/**").authenticated()
                .requestMatchers("/procesos", "/procesos/**").authenticated()
                .requestMatchers("/solicitudes", "/solicitudes/**").authenticated()
                .requestMatchers("/conversaciones", "/conversaciones/**").authenticated()
                .requestMatchers("/usuarios", "/usuarios/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            // Configuración de login web tradicional
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/app/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

