package excepciones;

public class ColaVaciaException extends Exception {

    public ColaVaciaException() {
        super("ERROR, La cola que busca acceder se encuentra vacia");
    }

    public ColaVaciaException(String mensaje) {
        super(mensaje);
    }
}