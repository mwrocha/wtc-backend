package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Group;
import br.com.wtc.domain.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // GET /api/groups — lista todos os grupos
    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups(
            @RequestParam(required = false) String divisionId,
            @RequestParam(required = false) String clientId
    ) {
        if (divisionId != null) {
            return ResponseEntity.ok(groupService.getGroupsByDivision(divisionId));
        }
        if (clientId != null) {
            return ResponseEntity.ok(groupService.getGroupsByClient(clientId));
        }
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    // GET /api/groups/{id} — busca grupo por ID (inclui clientIds)
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable String id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    // POST /api/groups — cria novo grupo
    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
        return ResponseEntity.ok(groupService.createGroup(group));
    }

    // PUT /api/groups/{id} — atualiza grupo
    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable String id,
            @RequestBody Group group
    ) {
        return ResponseEntity.ok(groupService.updateGroup(id, group));
    }

    // POST /api/groups/{id}/members/{clientId} — adiciona cliente ao grupo
    @PostMapping("/{id}/members/{clientId}")
    public ResponseEntity<Group> addMember(
            @PathVariable String id,
            @PathVariable String clientId
    ) {
        return ResponseEntity.ok(groupService.addClientToGroup(id, clientId));
    }

    // DELETE /api/groups/{id}/members/{clientId} — remove cliente do grupo
    @DeleteMapping("/{id}/members/{clientId}")
    public ResponseEntity<Group> removeMember(
            @PathVariable String id,
            @PathVariable String clientId
    ) {
        return ResponseEntity.ok(groupService.removeClientFromGroup(id, clientId));
    }
}