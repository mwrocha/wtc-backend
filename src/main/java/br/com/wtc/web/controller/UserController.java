package br.com.wtc.web.controller;

import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Buscar usuário por email (já existente)
    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(Map.of(
                        "id",    u.getId(),
                        "name",  u.getName(),
                        "email", u.getEmail(),
                        "role",  u.getRole()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // Alterar email
    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String newEmail  = body.get("newEmail");
        String password  = body.get("password");

        if (newEmail == null || newEmail.isBlank() || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Dados inválidos"));
        }

        // Verifica se o novo email já está em uso
        if (userRepository.existsByEmail(newEmail)) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Este e-mail já está em uso"));
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        // Confirma a senha antes de trocar o email
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Senha incorreta"));
        }

        user.setEmail(newEmail);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "E-mail alterado com sucesso"));
    }

    // Alterar senha
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Dados inválidos"));
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Senha atual incorreta"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
    }
}