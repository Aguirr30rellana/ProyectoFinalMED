package controlador;

import excepciones.CampoVacioException;
import excepciones.CategoriaException;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;
import modelo.servicios.ArbolCategorias;

import java.util.List;


public class ControladorCategorias {

    private final ArbolCategorias arbol;

    public ControladorCategorias(ArbolCategorias arbol) {
        this.arbol = arbol;
    }

    public NodoCategoria getRaiz() { return arbol.getRaiz(); }

    public NodoCategoria agregarSubcategoria(NodoCategoria padre, String nombre)
            throws CampoVacioException, CategoriaException {
        return arbol.agregarCategoria(padre, nombre);
    }

    // No se permite eliminar una rama que todavia tiene tareas
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

    // Calcula el total de tareas consultando directamente desde la raiz del arbol
    public int totalTareas()                           { return arbol.contarTareas(arbol.getRaiz()); }
}