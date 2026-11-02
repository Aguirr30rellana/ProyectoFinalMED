package modelo.servicios;

import java.util.Stack;
import modelo.entidades.Accion;

public class PilaHistorial {
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