package modelo.servicios;

import excepciones.CampoVacioException;
import excepciones.CategoriaException;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;

import java.util.ArrayList;
import java.util.List;

/*
 * Arbol general (jerarquico) de categorias y subcategorias.
 *
 * Casi todo es RECURSIVO y sigue el mismo patron:
 *   1. Procesar el nodo actual.
 *   2. Llamarse a si mismo con cada hijo.
 *   3. Caso base: un nodo hoja (sin hijos) -> el for no se ejecuta y la llamada termina.
 *
 * Los metodos que necesitan tareas las reciben como parametro (List<Tarea>),
 * asi el arbol no depende del GestorTareas.
 */
public class ArbolCategorias {

    private final NodoCategoria raiz;

    public ArbolCategorias(String nombreRaiz) {
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
            throw new CampoVacioException("nombre de categoria");
        }
        nombre = nombre.trim();

        if (buscar(nombre) != null) {
            throw new CategoriaException("Ya existe una categoria llamada \"" + nombre + "\".");
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

    public NodoCategoria buscar(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return null;
        return buscarRecursivo(raiz, nombre.trim());
    }

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

    public String mostrarConSangria(List<Tarea> tareas) {
        StringBuilder sb = new StringBuilder();
        mostrarRecursivo(raiz, 0, tareas, sb);
        return sb.toString();
    }

    private void mostrarRecursivo(NodoCategoria nodo, int nivel, List<Tarea> tareas, StringBuilder sb) {
        // 1. Nodo actual: 4 espacios por cada nivel de profundidad
        for (int i = 0; i < nivel; i++) {
            sb.append("    ");
        }
        sb.append(nivel == 0 ? "" : "└─ ")
        .append(nodo.getNombre())
        .append("  (").append(contarTareas(nodo, tareas)).append(")\n");

        // 2. Cada hijo, un nivel mas adentro
        for (NodoCategoria hijo : nodo.getHijos()) {
            mostrarRecursivo(hijo, nivel + 1, tareas, sb);
        }
    }


    //  RECURSION: contar tareas de una rama 

    // Tareas propias del nodo + las de todos sus descendientes
    public int contarTareas(NodoCategoria nodo, List<Tarea> tareas) {
        int total = contarTareasPropias(nodo, tareas);
        for (NodoCategoria hijo : nodo.getHijos()) {
            total += contarTareas(hijo, tareas);
        }
        return total;
    }

    private int contarTareasPropias(NodoCategoria nodo, List<Tarea> tareas) {
        int cantidad = 0;
        for (Tarea t : tareas) {
            if (nodo.contiene(t)) cantidad++;
        }
        return cantidad;
    }

    // Junta en una lista las tareas de toda la rama
    public List<Tarea> obtenerTareasDeRama(NodoCategoria nodo, List<Tarea> tareas) {
        List<Tarea> resultado = new ArrayList<>();
        recolectarRecursivo(nodo, tareas, resultado);
        return resultado;
    }

    private void recolectarRecursivo(NodoCategoria nodo, List<Tarea> tareas, List<Tarea> resultado) {
        for (Tarea t : tareas) {
            if (nodo.contiene(t)) resultado.add(t);
        }
        for (NodoCategoria hijo : nodo.getHijos()) {
            recolectarRecursivo(hijo, tareas, resultado);
        }
    }


    //  RECURSION: ruta y estadisticas 

    // Sube hasta la raiz:  Categorias › Trabajo › Proyecto A
    public String obtenerRuta(NodoCategoria nodo) {
        if (nodo.esRaiz()) {
            return nodo.getNombre();                             
        }
        return obtenerRuta(nodo.getPadre()) + " › " + nodo.getNombre();
    }

    public int contarCategorias(NodoCategoria nodo) {
        int total = 1;
        for (NodoCategoria hijo : nodo.getHijos()) {
            total += contarCategorias(hijo);
        }
        return total;
    }

    // Altura = cantidad de niveles (una hoja mide 1)
    public int altura(NodoCategoria nodo) {
        int mayor = 0;
        for (NodoCategoria hijo : nodo.getHijos()) {
            mayor = Math.max(mayor, altura(hijo));
        }
        return 1 + mayor;
    }

    // Todas las categorias en preorden (sirve para llenar un JComboBox al crear tareas)
    public List<NodoCategoria> listarCategorias() {
        List<NodoCategoria> lista = new ArrayList<>();
        listarRecursivo(raiz, lista);
        return lista;
    }

    private void listarRecursivo(NodoCategoria nodo, List<NodoCategoria> lista) {
        lista.add(nodo);
        for (NodoCategoria hijo : nodo.getHijos()) {
            listarRecursivo(hijo, lista);
        }
    }
}