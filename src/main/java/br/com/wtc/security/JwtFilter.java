package br.com.wtc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    // Construtor explícito — evita problema com Lombok + Spring em filtros
    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // ── Executa uma vez por request ───────────────────────────────────
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lê o header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Se não tem token ou não começa com "Bearer ", passa adiante
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (remove "Bearer ")
        final String token = authHeader.substring(7);

        // 4. Extrai o e-mail do token
        final String email = jwtUtil.extractEmail(token);

        // 5. Se tem e-mail e ainda não está autenticado nesta request
        if (email != null && SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            // 6. Carrega o usuário do MongoDB
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            // 7. Valida o token contra o usuário carregado
            if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {

                // 8. Cria o objeto de autenticação do Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // 9. Registra a autenticação no contexto da request
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        // 10. Continua para o próximo filtro / controller
        filterChain.doFilter(request, response);
    }
}