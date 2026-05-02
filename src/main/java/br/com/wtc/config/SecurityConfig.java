package br.com.wtc.config;

import br.com.wtc.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, UserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth

                // Público — login e registro
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                // Cliente pode buscar e atualizar o próprio perfil
                .requestMatchers(HttpMethod.GET, "/api/clients/{id}").authenticated().requestMatchers(HttpMethod.PUT, "/api/clients/{id}").authenticated()

                // Campanhas recebidas — cliente autenticado
                .requestMatchers("/api/campaigns-received/**").authenticated()

                // Campanhas — criação e disparo exclusivo do operador
                .requestMatchers("/api/campaigns/**").hasRole("OPERATOR")

                // Tarefas — exclusivo do operador
                .requestMatchers("/api/tasks/**").hasRole("OPERATOR")

                // Auditoria — exclusivo do operador
                .requestMatchers("/api/audit/**").hasRole("OPERATOR")

                // Solicitações de troca de grupo — cliente cria, operador gerencia
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/group-change-requests").hasRole("CLIENT").requestMatchers("/api/group-change-requests/**").hasRole("OPERATOR")

                // Resto de clientes e notas — exclusivo do operador
                .requestMatchers("/api/clients/**", "/api/notes/**").hasRole("OPERATOR")

                // Usuários — autenticado
                .requestMatchers("/api/users/**").authenticated()

                // Grupos, divisões e mensagens — autenticado
                .requestMatchers("/api/groups/**", "/api/divisions/**", "/api/messages/**").authenticated()

                // Todo o resto exige autenticação
                .anyRequest().authenticated()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authenticationProvider(authenticationProvider()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}