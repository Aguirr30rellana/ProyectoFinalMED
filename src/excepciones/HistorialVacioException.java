package excepciones;

public class HistorialVacioException extends Exception {
    
    public HistorialVacioException() {
        super("ERROR, No hay acciones para deshacer en la pila.");
    }

    public HistorialVacioException(String mensaje) {
        super(mensaje);
    }
}
 