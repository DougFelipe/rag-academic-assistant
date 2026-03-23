package br.ufrn.distribuida.ai.exception;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado.
 * 
 * <p>
 * Usada principalmente quando um professor não é encontrado por nome ou ID.
 * Resulta em resposta HTTP 404 Not Found.
 * 
 * @author Equipe DISTRIBUIDA 3
 * @since Sprint 5
 * @see br.ufrn.distribuida.ai.exception.GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Cria exceção com mensagem.
     * 
     * @param message Mensagem de erro
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Cria exceção com mensagem e causa.
     * 
     * @param message Mensagem de erro
     * @param cause   Causa raiz da exceção
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
