package excepciones;

public class CategoriaNoEncontradaException extends Exception {

    public CategoriaNoEncontradaException() {
        super("ERROR, La rama que busca acceder no existe o fue borrada");
    }

    public CategoriaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}