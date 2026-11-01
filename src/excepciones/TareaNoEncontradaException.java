package excepciones;

public class TareaNoEncontradaException extends Exception {
    public TareaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}