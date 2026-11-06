package excepciones;

public class TareaNoEncontradaException extends Exception {

    public TareaNoEncontradaException() {
        super("ERROR, La tarea a la que quiere acceder no existe o fue eliminada.");
    }

    public TareaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}