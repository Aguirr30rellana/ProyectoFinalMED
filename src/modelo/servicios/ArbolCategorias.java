package modelo.servicios;

import excepciones.CampoVacioException;
import excepciones.CategoriaException;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable; 

/*
 * Arbol general (jerarquico) de categorias y subcategorias.
 *
 * Casi todo es RECURSIVO y sigue el mismo patron:
 *   1. Procesar el nodo actual.
 *   2. Llamarse a si mismo con cada hijo.
 *   3. Caso base: un nodo hoja (sin hijos) -> el for no se ejecuta y la llamada termina.
 */

public class ArbolCategorias implements Serializable { 
    private static final long serialVersionUID = 1L; 

    private final NodoCategoria raiz;

    public ArbolCategorias(String nombreRaiz) 
    //Validacion propia para nombre de raiz
    throws CampoVacioException {
        raiz = new NodoCategoria(nombreRaiz, null);
    }

    public NodoCategoria getRaiz() { return raiz; }


    //  Agregar / eliminar 

    public NodoCategoria agregarCategoria(NodoCategoria padre, String nombre)
        throws CampoVacioException, CategoriaException {

            if (padre == null) {
                throw new CategoriaException("Seleccione primero la categoria padre.");
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new CampoVacioException("nombre de categoria vacio");
            }
            nombre = nombre.trim();

            if (buscarHijoDirecto(padre, nombre) != null) {
            throw new CategoriaException("Ya existe una subcategoria llamada \"" + nombre + "\" dentro de \"" + padre.getNombre() + "\".");
            }

            NodoCategoria nueva = new NodoCategoria(nombre, padre);
            padre.agregarHijo(nueva);
            return nueva;
    }

    // Crea la ruta completa si no existe: agregarPorRuta("Trabajo", "Proyecto A")
    public NodoCategoria agregarPorRuta(String... ruta) throws CampoVacioException, CategoriaException {
        NodoCategoria actual = raiz;
        for (String nombre : ruta) {
            NodoCategoria existente = buscarHijoDirecto(actual, nombre);
            actual = (existente != null) ? existente : agregarCategoria(actual, nombre);
        }
        return actual;
    }

    public void eliminarCategoria(NodoCategoria nodo) throws CategoriaException {
        if (nodo == null) {
            throw new CategoriaException("Seleccione la categoria a eliminar.");
        }
        if (nodo.esRaiz()) {
            throw new CategoriaException("No se puede eliminar la categoria raiz.");
        }
        nodo.getPadre().eliminarHijo(nodo);      
    }

    private NodoCategoria buscarHijoDirecto(NodoCategoria padre, String nombre) {
        for (NodoCategoria hijo : padre.getHijos()) {
            if (hijo.getNombre().equalsIgnoreCase(nombre)) return hijo;
        }
        return null;
    }


    // RECURSION: busqueda 

    //Valida y limpia el texto ingresado antes de recorrer desde la raiz
    public NodoCategoria buscar(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return null;
        return buscarRecursivo(raiz, nombre.trim());
    }

    //Evalua el nodo actual y de no coincidir delega a cada subcatgeoria 
    private NodoCategoria buscarRecursivo(NodoCategoria nodo, String nombre) {
        if (nodo.getNombre().equalsIgnoreCase(nombre)) {
            return nodo;                                        
        }
        for (NodoCategoria hijo : nodo.getHijos()) {
            NodoCategoria encontrado = buscarRecursivo(hijo, nombre);
            if (encontrado != null) return encontrado;
        }
        return null;                                              
    }


    //  RECURSION: mostrar con sangria 
    //generación del mapa visual del árbol desde la raiz
    public String mostrarConSangria() {
        StringBuilder sb = new StringBuilder();
        mostrarRecursivo(raiz, 0, sb);
        return sb.toString();
    }

    //Construye la representación jerárquica con profundidad
    private void mostrarRecursivo(NodoCategoria nodo, int nivel, StringBuilder sb) {
        // 1. Nodo actual: 4 espacios por cada nivel de profundidad
        for (int i = 0; i < nivel; i++) {
            sb.append("    ");
        }
        sb.append(nivel == 0 ? "" : "└─ ")
        .append(nodo.getNombre())
        .append("  (").append(contarTareas(nodo)).append(")\n");

        // 2. Cada hijo, un nivel mas adentro
        for (NodoCategoria hijo : nodo.getHijos()) {
            mostrarRecursivo(hijo, nivel + 1, sb);;
        }
    }


    //  RECURSION: contar tareas de una rama 

    // Tareas propias del nodo + las de todos sus descendientes
    //Utiliza la lista interna de tareas del nodo, que el nodo guarda como referencia 
    public int contarTareas(NodoCategoria nodo) {
        int total = nodo.getTareas().size();
        for (NodoCategoria hijo : nodo.getHijos()) {
            total += contarTareas(hijo);
        }
        return total;
    }

    // Junta en una lista las tareas de toda la rama de nodos 
    public List<Tarea> obtenerTareasDeRama(NodoCategoria nodo) {
        List<Tarea> resultado = new ArrayList<>();
        resultado.addAll(nodo.getTareas());
        for (NodoCategoria hijo : nodo.getHijos()) {
            resultado.addAll(obtenerTareasDeRama(hijo));
        }
        return resultado;
    }


    //  RECURSION: ruta y estadisticas 

    //Construye la ruta jerárquica completa recursivamente desde el nodo actual hasta la raíz.
    public String obtenerRuta(NodoCategoria nodo) {
        if (nodo.esRaiz()) {
            return nodo.getNombre();                             
        }
        return obtenerRuta(nodo.getPadre()) + " › " + nodo.getNombre();
    }

    //Calcula recursivamente la cantidad total de subcategorías que tiene una rama
    public int contarCategorias(NodoCategoria nodo) {
        int total = 1;
        for (NodoCategoria hijo : nodo.getHijos()) {
            total += contarCategorias(hijo);
        }
        return total;
    }

    // Calcula recursivamente la profundidad máxima desde un nodo hasta la ultima hoja
    public int altura(NodoCategoria nodo) {
        int mayor = 0;
        for (NodoCategoria hijo : nodo.getHijos()) {
            mayor = Math.max(mayor, altura(hijo));
        }
        return 1 + mayor;
    }

    // Devuelve una lista de todas las categorias en preorden, para facilitar la visualizacion 
    public List<NodoCategoria> listarCategorias() {
        List<NodoCategoria> lista = new ArrayList<>();
        listarRecursivo(raiz, lista);
        return lista;
    }

    //Devuelve u listado recursivamente de la rama desde el nodo actual, para facilitar la visualizacion
    private void listarRecursivo(NodoCategoria nodo, List<NodoCategoria> lista) {
        lista.add(nodo);
        for (NodoCategoria hijo : nodo.getHijos()) {
            listarRecursivo(hijo, lista);
        }
    }
}