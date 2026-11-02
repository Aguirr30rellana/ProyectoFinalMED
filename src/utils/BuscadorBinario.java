package utils;

import java.util.List;
import modelo.entidades.Tarea;

public class BuscadorBinario {

    public static Tarea buscarPorNombre(List<Tarea> listaTareas, String nombreBuscado) {
        if (listaTareas == null || nombreBuscado == null || listaTareas.isEmpty()) {
            return null;
        }

        int inicio = 0;
        int fin = listaTareas.size() - 1;

        while (inicio <= fin) {
            int medio = inicio + (fin - inicio) / 2;
            Tarea tareaMedio = listaTareas.get(medio);

            int comparacion = tareaMedio.getNombre().compareToIgnoreCase(nombreBuscado);

            if (comparacion == 0) {
                return tareaMedio;
            } else if (comparacion < 0) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }

        return null;
    }
}