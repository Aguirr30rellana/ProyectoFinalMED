package excepciones;

public class FechaInvalidaException extends Exception {

     public FechaInvalidaException() {
        super("ERROR, La fecha ingresada no tiene el formato adecuado");
    }

    public FechaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
