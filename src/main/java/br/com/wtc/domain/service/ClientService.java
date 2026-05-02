package br.com.wtc.domain.service;

import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.web.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientService {

    private static final String CLIENT_ROLE = "CLIENT";

    private final UserRepository userRepository;

    public ClientService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllClients() {
        return userRepository.findByRole(CLIENT_ROLE);
    }

    public User getClientById(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("Cliente não encontrado: " + id));
        if (!CLIENT_ROLE.equals(user.getRole())) {
            throw new BusinessException("Usuário não é um cliente: " + id);
        }
        return user;
    }

    public List<User> searchByName(String name) {
        return userRepository.findByRoleAndNameContainingIgnoreCase(CLIENT_ROLE, name);
    }

    public List<User> getClientsByGroup(String groupId) {
        return userRepository.findByRoleAndGroupId(CLIENT_ROLE, groupId);
    }

    public List<User> getClientsByDivision(String divisionId) {
        return userRepository.findByRoleAndDivisionId(CLIENT_ROLE, divisionId);
    }

    public List<User> getClientsByTag(String tag) {
        return userRepository.findByRoleAndTagsContaining(CLIENT_ROLE, tag);
    }

    public User updateClient(String id, User updated) {
        User client = getClientById(id);
        client.setName(updated.getName());
        client.setPhone(updated.getPhone());
        client.setCpf(updated.getCpf());
        client.setCompany(updated.getCompany());
        client.setStatus(updated.getStatus());
        client.setScore(updated.getScore());
        client.setTags(updated.getTags() != null ? updated.getTags() : client.getTags() != null ? client.getTags() : List.of());
        client.setDivisionId(updated.getDivisionId());
        client.setGroupId(updated.getGroupId());
        client.setUpdatedAt(LocalDateTime.now());
        // ── fcmToken NUNCA é sobrescrito via PUT ──────────────────────────────
        // O token é gerenciado exclusivamente pelo endpoint /api/users/me/fcm-token
        // para evitar que um token de um aparelho seja atribuído a outro usuário
        return userRepository.save(client);
    }
}