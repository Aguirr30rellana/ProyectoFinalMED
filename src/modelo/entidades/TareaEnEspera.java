package modelo.entidades;

import excepciones.CampoVacioException;
import excepciones.FechaInvalidaException;
import excepciones.PrioridadInvalidaException;
import utils.ConvertorFecha;

import java.time.LocalDate;


public class TareaEnEspera {

    private final String nombre;
    private final String descripcion;
    private final int prioridad;
    private final LocalDate fechaLimite;

    // Se valida al recibirla, para que no entren datos incorrectos a la cola
    public TareaEnEspera(String nombre, String descripcion, int prioridad, String fechaTexto)
            throws CampoVacioException, PrioridadInvalidaException, FechaInvalidaException {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CampoVacioException("nombre");
        }
        if (prioridad < Tarea.PRIORIDAD_MIN || prioridad > Tarea.PRIORIDAD_MAX) {
            throw new PrioridadInvalidaException("La prioridad debe estar entre "
                    + Tarea.PRIORIDAD_MIN + " y " + Tarea.PRIORIDAD_MAX + ".");
        }
        LocalDate fecha = ConvertorFecha.parsearFecha(fechaTexto);
        if (fecha.isBefore(LocalDate.now())) {
            throw new FechaInvalidaException("La fecha limite no puede ser una fecha pasada.");
        }

        this.nombre = nombre.trim();
        this.descripcion = (descripcion == null) ? "" : descripcion.trim();
        this.prioridad = prioridad;
        this.fechaLimite = fecha;
    }

    public String getNombre()         { return nombre; }
    public String getDescripcion()    { return descripcion; }
    public int getPrioridad()         { return prioridad; }
    public LocalDate getFechaLimite() { return fechaLimite; }

    @Override
    public String toString() { return nombre; }
}