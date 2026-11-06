package excepciones;

public class ColaVaciaException extends Exception {
    public ColaVaciaException(String mensaje) {
        super(mensaje);
    }
}