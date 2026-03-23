package br.ufrn.distribuida.ai.service;

import br.ufrn.distribuida.ai.dto.ProfessorInfo;
import br.ufrn.distribuida.ai.exception.ResourceNotFoundException;
import br.ufrn.distribuida.ai.model.Professor;
import br.ufrn.distribuida.ai.repository.ProfessorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP Internal Service for managing professor information.
 * This service provides methods to retrieve and search professor data
 * from the in-memory repository.
 */
@Service
@Slf4j
public class MCPInternalService {
    
    private final ProfessorRepository professorRepository;
    
    @Autowired
    public MCPInternalService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }
    
    /**
     * Gets detailed information about a professor by name or ID.
     * 
     * @param nameOrId Professor name or ID
     * @return ProfessorInfo with complete details
     * @throws ResourceNotFoundException if professor not found
     */
    public ProfessorInfo getProfessorInfo(String nameOrId) {
        log.debug("Getting professor info for: {}", nameOrId);
        
        Professor professor = professorRepository.findByName(nameOrId)
                .or(() -> professorRepository.findById(nameOrId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professor not found: " + nameOrId
                ));
        
        log.debug("Found professor: {} ({})", professor.getName(), professor.getId());
        return ProfessorInfo.from(professor);
    }
    
    /**
     * Gets a list of all professor names.
     * 
     * @return List of professor names
     */
    public List<String> getAllProfessorNames() {
        log.debug("Getting all professor names");
        
        List<String> names = professorRepository.findAll().stream()
                .map(Professor::getName)
                .sorted()
                .collect(Collectors.toList());
        
        log.debug("Returning {} professor names", names.size());
        return names;
    }
    
    /**
     * Searches professors by research area or keyword.
     * 
     * @param area Research area or keyword to search
     * @return List of matching professors
     */
    public List<ProfessorInfo> searchByResearchArea(String area) {
        log.debug("Searching professors by area: {}", area);
        
        List<ProfessorInfo> results = professorRepository.searchByKeyword(area).stream()
                .map(ProfessorInfo::from)
                .collect(Collectors.toList());
        
        log.debug("Found {} professors matching area: {}", results.size(), area);
        return results;
    }
    
    /**
     * Gets all professors with full information.
     * 
     * @return List of all professors
     */
    public List<ProfessorInfo> getAllProfessors() {
        log.debug("Getting all professors with full info");
        
        return professorRepository.findAll().stream()
                .map(ProfessorInfo::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the total count of professors in the system.
     * 
     * @return Number of professors
     */
    public int getProfessorCount() {
        return professorRepository.count();
    }
}
