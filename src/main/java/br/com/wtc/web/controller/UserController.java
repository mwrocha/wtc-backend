package br.com.wtc.web.controller;

import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.infra.MinioStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final MinioStorageService storageService;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          MinioStorageService storageService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService  = storageService;
    }

    // ── GET /api/users/by-email/{email} ───────────────────────────────────────
    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(Map.of(
                        "id",        u.getId(),
                        "name",      u.getName(),
                        "email",     u.getEmail(),
                        "role",      u.getRole(),
                        // Retorna URL pré-assinada do avatar se existir
                        "avatarKey", u.getAvatarKey() != null ? u.getAvatarKey() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /api/users/avatar ────────────────────────────────────────────────
    // Faz upload da foto de perfil para o R2 e salva a chave no MongoDB
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Arquivo não enviado ou vazio."));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        try {
            // Remove avatar antigo do R2 se existir
            if (user.getAvatarKey() != null && !user.getAvatarKey().isBlank()) {
                storageService.delete(user.getAvatarKey());
            }

            // Faz upload do novo avatar
            String objectKey = storageService.upload(file);

            // Gera URL pré-assinada para retornar ao app
            String presignedUrl = storageService.generatePresignedUrl(objectKey);

            // Salva a chave no MongoDB
            user.setAvatarKey(objectKey);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "objectKey", objectKey,
                    "url",       presignedUrl
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao fazer upload do avatar."));
        }
    }

    // ── GET /api/users/avatar ─────────────────────────────────────────────────
    // Retorna URL pré-assinada do avatar do usuário logado
    @GetMapping("/avatar")
    public ResponseEntity<?> getMyAvatar(
            @AuthenticationPrincipal UserDetails userDetails) {

        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> {
                    if (user.getAvatarKey() == null || user.getAvatarKey().isBlank())
                        return ResponseEntity.ok(Map.of("url", ""));

                    try {
                        String url = storageService.generatePresignedUrl(user.getAvatarKey());
                        return ResponseEntity.ok(Map.of(
                                "url",       url,
                                "objectKey", user.getAvatarKey()
                        ));
                    } catch (Exception e) {
                        return ResponseEntity.ok(Map.of("url", ""));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE /api/users/avatar ──────────────────────────────────────────────
    // Remove a foto de perfil do R2 e limpa o campo no MongoDB
    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteAvatar(
            @AuthenticationPrincipal UserDetails userDetails) {

        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> {
                    if (user.getAvatarKey() != null && !user.getAvatarKey().isBlank()) {
                        storageService.delete(user.getAvatarKey());
                        user.setAvatarKey(null);
                        userRepository.save(user);
                    }
                    return ResponseEntity.ok(Map.of("message", "Avatar removido com sucesso."));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String newEmail = body.get("newEmail");
        String password = body.get("password");

        if (newEmail == null || newEmail.isBlank() || password == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Dados inválidos"));

        if (userRepository.existsByEmail(newEmail))
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Este e-mail já está em uso"));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword()))
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Senha incorreta"));

        user.setEmail(newEmail);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "E-mail alterado com sucesso"));
    }

    // ── POST /api/users/change-password ──────────────────────────────────────
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Dados inválidos"));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Senha atual incorreta"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
    }
}