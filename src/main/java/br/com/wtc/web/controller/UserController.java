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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioStorageService storageService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, MinioStorageService storageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email).map(u -> ResponseEntity.ok(Map.of("id", u.getId(), "name", u.getName(), "email", u.getEmail(), "role", u.getRole(), "avatarKey", u.getAvatarKey() != null ? u.getAvatarKey() : ""))).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/me/name")
    public ResponseEntity<?> updateMyName(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String name = body.get("name");
        if (name == null || name.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Nome não pode ser vazio."));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        user.setName(name.trim());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("id", user.getId(), "name", user.getName()));
    }

    // ── PATCH /api/users/me/profile ───────────────────────────────────────────
    @PatchMapping("/me/profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        if (body.containsKey("name") && !body.get("name").isBlank()) user.setName(body.get("name").trim());
        if (body.containsKey("cpf")) user.setCpf(body.get("cpf"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("company")) user.setCompany(body.get("company"));

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Perfil atualizado com sucesso.", "name", user.getName() != null ? user.getName() : "", "cpf", user.getCpf() != null ? user.getCpf() : "", "phone", user.getPhone() != null ? user.getPhone() : "", "company", user.getCompany() != null ? user.getCompany() : ""));
    }

    @PatchMapping("/me/fcm-token")
    public ResponseEntity<?> updateFcmToken(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String fcmToken = body.get("fcmToken");
        if (fcmToken == null || fcmToken.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "fcmToken não pode ser vazio."));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        userRepository.findAllByFcmToken(fcmToken).forEach(other -> {
            if (!other.getId().equals(userOpt.get().getId())) {
                other.setFcmToken(null);
                userRepository.save(other);
            }
        });

        User user = userOpt.get();
        user.setFcmToken(fcmToken);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "FCM token atualizado."));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {

        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Arquivo não enviado ou vazio."));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        try {
            if (user.getAvatarKey() != null && !user.getAvatarKey().isBlank())
                storageService.delete(user.getAvatarKey());

            String objectKey = storageService.upload(file);
            String presignedUrl = storageService.generatePresignedUrl(objectKey);
            user.setAvatarKey(objectKey);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("objectKey", objectKey, "url", presignedUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao fazer upload do avatar."));
        }
    }

    @GetMapping("/avatar")
    public ResponseEntity<?> getMyAvatar(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).map(user -> {
            if (user.getAvatarKey() == null || user.getAvatarKey().isBlank())
                return ResponseEntity.ok(Map.of("url", ""));
            try {
                String url = storageService.generatePresignedUrl(user.getAvatarKey());
                return ResponseEntity.ok(Map.of("url", url, "objectKey", user.getAvatarKey()));
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("url", ""));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteAvatar(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).map(user -> {
            if (user.getAvatarKey() != null && !user.getAvatarKey().isBlank()) {
                storageService.delete(user.getAvatarKey());
                user.setAvatarKey(null);
                userRepository.save(user);
            }
            return ResponseEntity.ok(Map.of("message", "Avatar removido com sucesso."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String newEmail = body.get("newEmail");
        String password = body.get("password");

        if (newEmail == null || newEmail.isBlank() || password == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Dados inválidos"));

        if (userRepository.existsByEmail(newEmail))
            return ResponseEntity.status(409).body(Map.of("message", "Este e-mail já está em uso"));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Senha incorreta"));

        user.setEmail(newEmail);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "E-mail alterado com sucesso"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Dados inválidos"));

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Senha atual incorreta"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
    }
}