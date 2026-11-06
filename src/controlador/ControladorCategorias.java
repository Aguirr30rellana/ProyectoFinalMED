package controlador;

import excepciones.CampoVacioException;
import excepciones.CategoriaException;
import excepciones.FechaInvalidaException;
import excepciones.PrioridadInvalidaException;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;
import modelo.servicios.ArbolCategorias;
import modelo.servicios.GestorTareas;

import java.util.List;

/*
 * Une el arbol de categorias con el GestorTareas.
 * La vista solo habla con este controlador; las excepciones suben
 * hasta la vista para mostrarlas en un mensaje.
 */
public class ControladorCategorias {

    private final ArbolCategorias arbol;
    private final GestorTareas gestor;

    public ControladorCategorias(ArbolCategorias arbol, GestorTareas gestor) {
        this.arbol = arbol;
        this.gestor = gestor;
    }

    public NodoCategoria getRaiz() { return arbol.getRaiz(); }

    public NodoCategoria agregarSubcategoria(NodoCategoria padre, String nombre)
            throws CampoVacioException, CategoriaException {
        return arbol.agregarCategoria(padre, nombre);
    }

    public void eliminarCategoria(NodoCategoria nodo) throws CategoriaException {
        if (nodo != null && !nodo.esRaiz()) {
            int cantidad = contarTareas(nodo);
            if (cantidad > 0) {
                throw new CategoriaException("La rama \"" + nodo.getNombre() + "\" tiene " + cantidad
                        + " tarea(s). Eliminalas o cambialas de categoria primero.");
            }
        }
        arbol.eliminarCategoria(nodo);
    }

    public NodoCategoria buscar(String nombre) throws CategoriaException {
        NodoCategoria encontrada = arbol.buscar(nombre);
        if (encontrada == null) {
            throw new CategoriaException("No se encontro la categoria \"" + nombre + "\".");
        }
        return encontrada;
    }


    public int contarTareas(NodoCategoria nodo)        { return arbol.contarTareas(nodo); }
    public List<Tarea> tareasDeRama(NodoCategoria n)   { return arbol.obtenerTareasDeRama(n); }
    public String ruta(NodoCategoria nodo)             { return arbol.obtenerRuta(nodo); }
    public int totalCategorias()                       { return arbol.contarCategorias(arbol.getRaiz()) - 1; } 
    
    // Calcula el total de tareas consultando directamente desde la raíz del árbol
    public int totalTareas()                           { return arbol.contarTareas(arbol.getRaiz()); }
}