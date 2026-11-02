package excepciones;

public class CategoriaExistenteException extends Exception {
    public CategoriaExistenteException(String mensaje) {
        super(mensaje);
    }
}