package utils;

import excepciones.FechaInvalidaException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
 
public class ConvertorFecha {
 
    // Convierte texto yyyy-MM-dd a LocalDate o lanza FechaInvalidaException.

    public static LocalDate parsearFecha(String texto) throws FechaInvalidaException {
        //fecha vacia
        if (texto == null || texto.trim().isEmpty()) {
            throw new FechaInvalidaException("La fecha no puede estar vacia.");
        }
        //conversion de fecha
        try {
            return LocalDate.parse(texto.trim());

        } catch (DateTimeParseException e) {
            throw new FechaInvalidaException("Fecha invalida. Formato requerido: yyyy-MM-dd ");
        }
    }
}