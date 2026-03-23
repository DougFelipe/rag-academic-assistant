package br.ufrn.distribuida.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de Resposta com Lista de Professores.
 * 
 * <p>Retorna lista de nomes de professores com contagem total.
 * Usado pelo endpoint GET /api/ai/professors.
 * 
 * @author Equipe DISTRIBUIDA 3
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorListResponse {
    
    /** Lista de nomes de professores */
    private List<String> professorNames;
    
    /** Quantidade total de professores */
    private int total;
    
    /**
     * Cria resposta a partir de lista de nomes.
     * 
     * @param names Lista de nomes de professores
     * @return Resposta com nomes e total
     */
    public static ProfessorListResponse of(List<String> names) {
        return ProfessorListResponse.builder()
                .professorNames(names)
                .total(names.size())
                .build();
    }
}
