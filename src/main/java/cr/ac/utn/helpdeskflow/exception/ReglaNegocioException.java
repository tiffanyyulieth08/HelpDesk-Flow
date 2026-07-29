package cr.ac.utn.helpdeskflow.exception;

/** Excepción utilizada cuando una operación viola una regla del dominio. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
