package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Group;
import br.com.wtc.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    // Lista todos os grupos
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // Busca grupo por ID
    public Group getGroupById(String id) {
        return groupRepository.findById(id).orElseThrow(() -> new RuntimeException("Grupo não encontrado: " + id));
    }

    // Lista grupos de uma divisão específica
    public List<Group> getGroupsByDivision(String divisionId) {
        return groupRepository.findByDivisionId(divisionId);
    }

    // Lista grupos que um cliente pertence
    public List<Group> getGroupsByClient(String clientId) {
        return groupRepository.findByClientIdsContaining(clientId);
    }

    // Adiciona um cliente a um grupo
    public Group addClientToGroup(String groupId, String clientId) {
        Group group = getGroupById(groupId);
        if (!group.getClientIds().contains(clientId)) {
            group.getClientIds().add(clientId);
            groupRepository.save(group);
        }
        return group;
    }

    // Remove um cliente de um grupo
    public Group removeClientFromGroup(String groupId, String clientId) {
        Group group = getGroupById(groupId);
        group.getClientIds().remove(clientId);
        groupRepository.save(group);
        return group;
    }

    // Cria um novo grupo
    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    // Atualiza um grupo existente
    public Group updateGroup(String id, Group updated) {
        Group group = getGroupById(id);
        group.setName(updated.getName());
        group.setDescription(updated.getDescription());
        group.setDivisionId(updated.getDivisionId());
        return groupRepository.save(group);
    }
}