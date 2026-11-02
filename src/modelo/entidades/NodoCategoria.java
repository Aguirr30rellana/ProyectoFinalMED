package modelo.entidades;

import java.util.ArrayList;
import java.util.List;

import excepciones.CampoVacioException;

public class NodoCategoria {

    //atributos
    private String nombre; 
    private NodoCategoria padre; 
    private final ArrayList<NodoCategoria> hijos = new ArrayList<>();
    
    //El nodo guarda referencias a sus tareas
    private final ArrayList<Tarea> tareas = new ArrayList<>();

    //contructor
    public NodoCategoria(String nombre, NodoCategoria padre) throws CampoVacioException {
        //usa la validacion para insertar el nombre
        setNombre(nombre);
        this.padre = padre;
    }

    // validacion de entrada con excepcion propia, nombre no puede estar vacio o lleno de espacios
    public void setNombre(String nombre) throws CampoVacioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CampoVacioException("El nombre de la categoria");
        }
        this.nombre = nombre.trim();
    }
    
    public void setPadre(NodoCategoria padre) {
        this.padre = padre;
    }


    public String getNombre() { return nombre; }
    public NodoCategoria getPadre() { return padre; }
    public List<NodoCategoria> getHijos() { return hijos; }
    public List<Tarea> getTareas() { return tareas; } 


    //Elementos de la lista hijo
    public void agregarHijo(NodoCategoria hijo) { hijos.add(hijo); }

    public void eliminarHijo(NodoCategoria hijo) { hijos.remove(hijo); }
    
    //Obtener estados
    public boolean esRaiz() { return padre == null; }

    public boolean esHoja() { return hijos.isEmpty(); }

    // Elementos de listado tareas
    public void agregarTarea(Tarea tarea) {
        if (tarea != null && !tareas.contains(tarea)) tareas.add(tarea);
    }
    
    public void eliminarTarea(Tarea tarea) {
        tareas.remove(tarea);
    }


    // Construccion de ruta completa como identificador
    public String obtenerRutaCompleta() {
        //retorna raiz
        if (padre == null) return nombre;
        //Devuelve la ruta jerarquica 
        return padre.obtenerRutaCompleta() + " › " + nombre;
    }

    // Comparacion por ruta completa y de la pertenecia de una tarea al nodo
    public boolean contiene(Tarea tarea) {
        return tarea.getCategoria() != null && 
               tarea.getCategoria().equalsIgnoreCase(this.obtenerRutaCompleta());
    }

    @Override
    public String toString() { return nombre; }
}