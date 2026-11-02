package modelo.servicios;

import java.util.LinkedList;
import java.util.Queue;
import modelo.entidades.Tarea;

public class ColaEspera {
    private Queue<Tarea> cola;

    public ColaEspera() {
        this.cola = new LinkedList<>();
    }

    public void encolar(Tarea tarea) {
        if (tarea != null) {
            cola.add(tarea);
        }
    }

    public Tarea atenderSiguiente() {
        return cola.poll();
    }

    public boolean estaVacia() {
        return cola.isEmpty();
    }

    public int obtenerCantidad() {
        return cola.size();
    }
}