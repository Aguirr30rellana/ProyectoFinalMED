package excepciones;

public class CampoVacioException extends Exception {
    public CampoVacioException(String campo) {
        super("El campo '" + campo + "' no puede estar vacio.");
    }
}