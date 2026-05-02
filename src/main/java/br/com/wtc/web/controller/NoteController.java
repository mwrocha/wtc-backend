package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Note;
import br.com.wtc.domain.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // PUT /api/notes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable String id, @RequestBody java.util.Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.update(id, userDetails.getUsername(), body.get("content")));
    }

    // DELETE /api/notes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        noteService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}