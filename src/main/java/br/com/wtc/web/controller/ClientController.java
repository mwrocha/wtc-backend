package br.com.wtc.web.controller;

import br.com.wtc.domain.model.User;
import br.com.wtc.domain.service.ClientService;
import br.com.wtc.domain.service.NoteService;
import br.com.wtc.domain.model.Note;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final NoteService noteService;

    public ClientController(ClientService clientService, NoteService noteService) {
        this.clientService = clientService;
        this.noteService = noteService;
    }

    // GET /api/clients — lista todos com filtros opcionais
    @GetMapping
    public ResponseEntity<List<User>> getClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String divisionId
    ) {
        if (name != null)       return ResponseEntity.ok(clientService.searchByName(name));
        if (tag != null)        return ResponseEntity.ok(clientService.getClientsByTag(tag));
        if (groupId != null)    return ResponseEntity.ok(clientService.getClientsByGroup(groupId));
        if (divisionId != null) return ResponseEntity.ok(clientService.getClientsByDivision(divisionId));
        return ResponseEntity.ok(clientService.getAllClients());
    }

    // GET /api/clients/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getClientById(@PathVariable String id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    // PUT /api/clients/{id}
    @PutMapping("/{id}")
    public ResponseEntity<User> updateClient(
            @PathVariable String id,
            @RequestBody User client
    ) {
        return ResponseEntity.ok(clientService.updateClient(id, client));
    }

    // GET /api/clients/{id}/notes
    @GetMapping("/{id}/notes")
    public ResponseEntity<List<Note>> getNotes(@PathVariable String id) {
        return ResponseEntity.ok(noteService.findByClient(id));
    }

    // POST /api/clients/{id}/notes
    @PostMapping("/{id}/notes")
    public ResponseEntity<Note> addNote(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String operatorId = userDetails.getUsername(); // email do operador logado
        String content = body.get("text");
        return ResponseEntity.ok(noteService.create(id, operatorId, content));
    }
}