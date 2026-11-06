package modelo.servicios;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import modelo.entidades.TareaEnEspera;

//la primera tarea que se recibe es la primera que se asigna
public class ColaEspera {
    private Queue<TareaEnEspera> cola;

    public ColaEspera() {
        this.cola = new LinkedList<>();
    }

    // Entra al final de la cola
    public void encolar(TareaEnEspera tarea) {
        if (tarea != null) {
            cola.add(tarea);
        }
    }

    // Sale la del frente (la mas antigua)
    public TareaEnEspera atenderSiguiente() {
        return cola.poll();
    }

    // Consulta la del frente sin sacarla
    public TareaEnEspera verSiguiente() {
        return cola.peek();
    }

    public boolean estaVacia() {
        return cola.isEmpty();
    }

    public int obtenerCantidad() {
        return cola.size();
    }

    // Copia en orden de llegada, solo para mostrarla (no modifica la cola)
    public List<TareaEnEspera> obtenerTodas() {
        return new ArrayList<>(cola);
    }
}