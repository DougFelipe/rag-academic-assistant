package br.ufrn.distribuida.ai.dto;

import br.ufrn.distribuida.ai.model.Professor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de Informações de Professor retornado pela API.
 * 
 * <p>
 * Usado para transferir dados de professores entre camadas.
 * Converte entidade {@link Professor} para formato de resposta.
 * 
 * @author Equipe DISTRIBUIDA 3
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorInfo {

    private String id;
    private String name;
    private String department;
    private String email;
    private String office;
    private String phone;
    private String content;

    /**
     * Cria um DTO ProfessorInfo a partir de uma entidade Professor.
     * 
     * @param professor Entidade Professor fonte
     * @return DTO ProfessorInfo populado
     */
    public static ProfessorInfo from(Professor professor) {
        return ProfessorInfo.builder()
                .id(professor.getId())
                .name(professor.getName())
                .department(professor.getDepartment())
                .email(professor.getEmail())
                .office(professor.getOffice())
                .phone(professor.getPhone())
                .content(professor.getContent())
                .build();
    }
}
