package excepciones;

public class PrioridadInvalidaException extends Exception {

    public PrioridadInvalidaException() {
        super("ERROR, Ingreso una prioridad fuera del rango del sistema.");
    }

    public PrioridadInvalidaException(String mensaje) {
        super(mensaje);
    }
}
 