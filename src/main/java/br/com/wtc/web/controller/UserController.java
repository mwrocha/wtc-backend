package br.com.wtc.web.controller;

import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/users/by-email/{email}
    // Retorna nome e id de qualquer usuário pelo email — acessível por todos autenticados
    // Usado pelo cliente para exibir o nome do operador no chat
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserPublicDto> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(
                        new UserPublicDto(user.getId(), user.getName(), user.getEmail(), user.getRole())
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    public record UserPublicDto(String id, String name, String email, String role) {}
}