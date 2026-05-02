package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Division;
import br.com.wtc.domain.service.DivisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
public class DivisionController {

    private final DivisionService divisionService;

    public DivisionController(DivisionService divisionService) {
        this.divisionService = divisionService;
    }

    // GET /api/divisions — lista todas as divisões
    @GetMapping
    public ResponseEntity<List<Division>> getAllDivisions() {
        return ResponseEntity.ok(divisionService.getAllDivisions());
    }

    // GET /api/divisions/{id} — busca por ID
    @GetMapping("/{id}")
    public ResponseEntity<Division> getDivisionById(@PathVariable String id) {
        return ResponseEntity.ok(divisionService.getDivisionById(id));
    }

    // POST /api/divisions — cria nova divisão
    @PostMapping
    public ResponseEntity<Division> createDivision(@RequestBody Division division) {
        return ResponseEntity.ok(divisionService.createDivision(division));
    }

    // PUT /api/divisions/{id} — atualiza divisão
    @PutMapping("/{id}")
    public ResponseEntity<Division> updateDivision(@PathVariable String id, @RequestBody Division division) {
        return ResponseEntity.ok(divisionService.updateDivision(id, division));
    }

    // DELETE /api/divisions/{id} — remove divisão
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDivision(@PathVariable String id) {
        divisionService.deleteDivision(id);
        return ResponseEntity.noContent().build();
    }
}