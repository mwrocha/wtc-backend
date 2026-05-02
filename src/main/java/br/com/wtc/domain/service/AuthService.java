package br.com.wtc.domain.service;

import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.web.exception.BusinessException;
import br.com.wtc.security.JwtUtil;
import br.com.wtc.web.dto.LoginRequest;
import br.com.wtc.web.dto.LoginResponse;
import br.com.wtc.web.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // ── Limpa fcmToken duplicado ──────────────────────────────────
        String currentFcmToken = user.getFcmToken();
        if (currentFcmToken != null && !currentFcmToken.isBlank()) {
            userRepository.findAllByFcmToken(currentFcmToken).forEach(other -> {
                if (!other.getId().equals(user.getId())) {
                    other.setFcmToken(null);
                    userRepository.save(other);
                }
            });
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado: " + request.email());
        }

        if ("CLIENT".equals(request.role())) {
            if (request.cpf() == null || request.cpf().isBlank())
                throw new BusinessException("CPF é obrigatório para clientes.");
            if (request.phone() == null || request.phone().isBlank())
                throw new BusinessException("Telefone é obrigatório para clientes.");
        }

        // ── Gera sessionToken no registro ─────────────────────────────
        String sessionToken = UUID.randomUUID().toString();

        User user = User.builder().name(request.name()).email(request.email()).password(passwordEncoder.encode(request.password())).role(request.role()).phone(request.phone()).cpf(request.cpf()).company(request.company()).sessionToken(sessionToken).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), sessionToken);
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}