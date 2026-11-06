package modelo.servicios;

import java.util.Stack;
import java.io.Serializable; 
import modelo.entidades.Accion;

public class PilaHistorial implements Serializable { 
    private static final long serialVersionUID = 1L; 

    private Stack<Accion> historial;

    public PilaHistorial() {
        this.historial = new Stack<>();
    }

    public void registrarAccion(Accion accion) {
        if (accion != null) {
            historial.push(accion);
        }
    }

    public Accion deshacerUltimaAccion() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.pop();
    }

    public boolean estaVacia() {
        return historial.isEmpty();
    }

    public int obtenerCantidad() {
        return historial.size();
    }
}