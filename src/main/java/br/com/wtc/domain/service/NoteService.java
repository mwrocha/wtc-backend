package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Note;
import br.com.wtc.domain.repository.NoteRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.web.exception.BusinessException;
import br.com.wtc.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;  // usa users em vez de clients

    public NoteService(NoteRepository noteRepository,
                       UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public Note create(String clientId, String operatorId, String content) {
        if (!userRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", clientId);
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException("Conteúdo da anotação é obrigatório");
        }
        Note note = Note.builder()
                .clientId(clientId)
                .operatorId(operatorId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return noteRepository.save(note);
    }

    public List<Note> findByClient(String clientId) {
        if (!userRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", clientId);
        }
        return noteRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public Note update(String noteId, String operatorId, String content) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Anotação", noteId));
        if (!note.getOperatorId().equals(operatorId)) {
            throw new BusinessException("Você não tem permissão para editar esta anotação");
        }
        note.setContent(content);
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public void delete(String noteId, String operatorId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Anotação", noteId));
        if (!note.getOperatorId().equals(operatorId)) {
            throw new BusinessException("Você não tem permissão para deletar esta anotação");
        }
        noteRepository.deleteById(noteId);
    }
}