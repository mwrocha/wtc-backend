package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Division;
import br.com.wtc.domain.repository.DivisionRepository;
import br.com.wtc.web.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DivisionService {

    private final DivisionRepository divisionRepository;

    public DivisionService(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    public List<Division> getAllDivisions() {
        return divisionRepository.findAll();
    }

    public Division getDivisionById(String id) {
        return divisionRepository.findById(id).orElseThrow(() -> new BusinessException("Divisão não encontrada: " + id));
    }

    public Division createDivision(Division division) {
        division.setCreatedAt(LocalDateTime.now());
        return divisionRepository.save(division);
    }

    public Division updateDivision(String id, Division updated) {
        Division division = getDivisionById(id);
        division.setName(updated.getName());
        division.setDescription(updated.getDescription());
        return divisionRepository.save(division);
    }

    public void deleteDivision(String id) {
        getDivisionById(id);
        divisionRepository.deleteById(id);
    }
}