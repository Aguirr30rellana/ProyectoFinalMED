package excepciones;

public class CampoVacioException extends Exception {

    public CampoVacioException() {
        super("ERROR, Se intento ingresar un  campo vacio");
    }

    public CampoVacioException(String campo) {
        super("El campo '" + campo + "' no puede estar vacio.");
    }

}