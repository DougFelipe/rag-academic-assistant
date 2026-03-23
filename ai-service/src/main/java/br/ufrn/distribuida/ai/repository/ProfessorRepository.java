package br.ufrn.distribuida.ai.repository;

import br.ufrn.distribuida.ai.model.Professor;
import br.ufrn.distribuida.ai.parser.MarkdownParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for managing Professor entities loaded from Markdown files.
 * Stores professors in memory for fast access.
 */
@Repository
@Slf4j
public class ProfessorRepository {
    
    private final Map<String, Professor> professorsById = new LinkedHashMap<>();
    private final Map<String, Professor> professorsByName = new LinkedHashMap<>();
    private final MarkdownParser parser;
    
    @Autowired
    public ProfessorRepository(MarkdownParser parser) {
        this.parser = parser;
    }
    
    /**
     * Loads all professor markdown files from classpath on startup.
     */
    @PostConstruct
    public void loadProfessors() {
        log.info("Starting to load professor data from Markdown files...");
        
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:data/professors/*.md");
            
            log.info("Found {} markdown files", resources.length);
            
            for (Resource resource : resources) {
                try {
                    Professor professor = parser.parseFromStream(
                        resource.getInputStream(), 
                        resource.getFilename()
                    );
                    
                    professorsById.put(professor.getId(), professor);
                    professorsByName.put(professor.getName().toLowerCase(), professor);
                    
                    log.info("Loaded professor: {} ({})", professor.getName(), professor.getId());
                } catch (Exception e) {
                    log.error("Failed to parse file: {}", resource.getFilename(), e);
                }
            }
            
            log.info("Successfully loaded {} professors", professorsById.size());
            
        } catch (IOException e) {
            log.error("Failed to load professors from resources", e);
        }
    }
    
    /**
     * Finds a professor by their unique ID.
     */
    public Optional<Professor> findById(String id) {
        return Optional.ofNullable(professorsById.get(id));
    }
    
    /**
     * Finds a professor by name (case-insensitive partial match).
     */
    public Optional<Professor> findByName(String name) {
        String searchName = name.toLowerCase();
        
        // Try exact match first
        Professor exact = professorsByName.get(searchName);
        if (exact != null) {
            return Optional.of(exact);
        }
        
        // Try partial match
        return professorsByName.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(searchName))
                .findFirst();
    }
    
    /**
     * Returns all loaded professors.
     */
    public List<Professor> findAll() {
        return new ArrayList<>(professorsById.values());
    }
    
    /**
     * Searches professors by keyword in their content.
     * 
     * @param keyword Search term (case-insensitive)
     * @return List of professors whose content contains the keyword
     */
    public List<Professor> searchByKeyword(String keyword) {
        String searchTerm = keyword.toLowerCase();
        
        return professorsById.values().stream()
                .filter(p -> p.getContent().toLowerCase().contains(searchTerm)
                        || p.getName().toLowerCase().contains(searchTerm)
                        || p.getDepartment().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the total count of loaded professors.
     */
    public int count() {
        return professorsById.size();
    }
}
