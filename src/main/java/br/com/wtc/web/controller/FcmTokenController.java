package br.com.wtc.web.controller;

import br.com.wtc.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class FcmTokenController {

    private final UserRepository userRepository;

    public FcmTokenController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // POST /api/users/fcm-token
    // Salva o FCM token do usuário autenticado
    @PostMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        userRepository.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            user.setFcmToken(token);
            userRepository.save(user);
        });

        return ResponseEntity.ok().build();
    }
}