package modelo.entidades;

import java.util.ArrayList;
import java.util.List;

/*
 * Nodo de un ARBOL GENERAL (n-ario): cada categoria puede tener
 * cualquier cantidad de subcategorias.
 *
 *   Categorias
 *    ├─ Trabajo
 *    │   └─ Proyecto A
 *    │       └─ Documentacion
 *    └─ Personal
 *
 * Las tareas NO se guardan aqui: la clase Tarea ya guarda el nombre de su
 * categoria (atributo categoria), asi que el arbol las cuenta comparando nombres.
 */
public class NodoCategoria {

    private final String nombre;
    private final NodoCategoria padre;                     
    private final ArrayList<NodoCategoria> hijos = new ArrayList<>();

    public NodoCategoria(String nombre, NodoCategoria padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public void agregarHijo(NodoCategoria hijo) { hijos.add(hijo); }
    public void eliminarHijo(NodoCategoria hijo) { hijos.remove(hijo); }

    public boolean esRaiz() { return padre == null; }
    public boolean esHoja() { return hijos.isEmpty(); }

    public String getNombre() { return nombre; }
    public NodoCategoria getPadre() { return padre; }
    public List<NodoCategoria> getHijos() { return hijos; }

    // Una tarea pertenece a este nodo si su categoria tiene el mismo nombre
    public boolean contiene(Tarea tarea) {
        return tarea.getCategoria() != null && tarea.getCategoria().equalsIgnoreCase(nombre);
    }

    @Override
    public String toString() { return nombre; }
}