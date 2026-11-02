package excepciones;

public class CategoriaNoEncontradaException extends Exception {
    public CategoriaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}