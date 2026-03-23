package br.ufrn.distribuida.ai.parser;

import br.ufrn.distribuida.ai.model.Professor;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Component responsible for parsing Markdown files with YAML frontmatter
 * to extract professor information.
 */
@Component
@Slf4j
public class MarkdownParser {
    
    private final Parser parser;
    
    public MarkdownParser() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
            YamlFrontMatterExtension.create()
        ));
        this.parser = Parser.builder(options).build();
        log.info("MarkdownParser initialized with YAML frontmatter support");
    }
    
    /**
     * Parses a Markdown file and extracts professor information.
     * 
     * @param filePath Path to the markdown file
     * @return Professor object with extracted data
     * @throws IOException if file cannot be read
     */
    public Professor parseFile(Path filePath) throws IOException {
        log.debug("Parsing file: {}", filePath);
        
        String content = Files.readString(filePath);
        return parseContent(content, filePath.getFileName().toString());
    }
    
    /**
     * Parses a Markdown content from an InputStream (for JAR resources).
     * 
     * @param inputStream InputStream of the markdown file
     * @param filename Name of the file for logging
     * @return Professor object with extracted data
     * @throws IOException if content cannot be read
     */
    public Professor parseFromStream(InputStream inputStream, String filename) throws IOException {
        log.debug("Parsing stream for file: {}", filename);
        
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        return parseContent(content, filename);
    }
    
    /**
     * Common parsing logic for both file and stream inputs.
     */
    private Professor parseContent(String content, String sourceFile) {
        Node document = parser.parse(content);
        
        // Extract YAML frontmatter
        AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
        visitor.visit(document);
        Map<String, List<String>> metadata = visitor.getData();
        
        // Extract markdown content (everything after the second ---)
        String markdownContent = extractMarkdownContent(content);
        
        Professor professor = Professor.builder()
                .id(getFirst(metadata, "id"))
                .name(getFirst(metadata, "nome_completo"))
                .department(getFirst(metadata, "departamento"))
                .email(getFirst(metadata, "email"))
                .office(getFirst(metadata, "endereco_profissional"))
                .phone(getFirst(metadata, "telefone"))
                .content(markdownContent)
                .sourceFile(sourceFile)
                .build();
        
        log.debug("Parsed professor: {} (ID: {})", professor.getName(), professor.getId());
        return professor;
    }
    
    /**
     * Extracts markdown content after YAML frontmatter.
     */
    private String extractMarkdownContent(String fullContent) {
        int firstDash = fullContent.indexOf("---");
        if (firstDash == -1) {
            return fullContent;
        }
        
        int secondDash = fullContent.indexOf("---", firstDash + 3);
        if (secondDash == -1) {
            return fullContent;
        }
        
        return fullContent.substring(secondDash + 3).trim();
    }
    
    /**
     * Gets the first value from metadata map or empty string if not found.
     */
    private String getFirst(Map<String, List<String>> map, String key) {
        List<String> values = map.get(key);
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.get(0);
    }
}
