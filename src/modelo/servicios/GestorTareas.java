package modelo.servicios;

import excepciones.CampoVacioException;
import excepciones.FechaInvalidaException;
import excepciones.PrioridadInvalidaException;
import excepciones.TareaNoEncontradaException;
import modelo.entidades.Tarea;
import utils.ConvertorFecha;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;



public class GestorTareas {

    private final ArrayList<Tarea> tareas_pendientes = new ArrayList<>();
    private final ArrayList<Tarea> tareas_completadas = new ArrayList<>();

    //identificador
    private int siguienteId = 1;
    

    //Consultas de listados
    public List<Tarea> getPendientes() { return new ArrayList<>(tareas_pendientes); }
    public List<Tarea> getCompletadas() { return new ArrayList<>(tareas_completadas); }


    //Consulta de existencia
    private Tarea buscarEnLista(List<Tarea> lista, int id) {
        
        //Recorrer todas las instancias de la lista
        for (Tarea tarea : lista) {
            if (tarea.getId() == id) return tarea;
        }

        return null;
    }
 
    //Creacion de tarea y validaciones
    public Tarea crearTarea(String nombre, String descripcion, int prioridad,  String fechaTexto, String categoria)
            
    throws FechaInvalidaException, PrioridadInvalidaException, CampoVacioException {

        //validacion
        LocalDate fecha = ConvertorFecha.parsearFecha(fechaTexto);
        if (fecha.isBefore(LocalDate.now())) {
            throw new FechaInvalidaException("ERROR, Ingreso una fecha pasada como fecha limite.");
        }
        //creacion de la instancia
        Tarea tarea_nueva = new Tarea(siguienteId, nombre, descripcion, prioridad, fecha, categoria);

        //identificador de la siguiente tarea
        siguienteId++;

        //agregado de la isntancia al listado
        tareas_pendientes.add(tarea_nueva);

        return tarea_nueva;
    }
    
    //Elimina Tareas del listado, devuelve el objeto tarea
    public Tarea eliminarTarea(int id) 
    
    throws TareaNoEncontradaException {
        //busqueda de instancia en ambas listas para eliminar tarea totalmente
        Tarea tareap = buscarEnLista(tareas_pendientes, id);
        if (tareap != null) {
            tareas_pendientes.remove(tareap);
            return tareap;
        }
        Tarea tareac = buscarEnLista(tareas_completadas, id);
        if (tareac != null) {
            tareas_completadas.remove(tareac);
            return tareac;
        }

        throw new TareaNoEncontradaException("La tarea que quiere eliminar no existe o ya fue eliminada.");
    }


    //Tarea pendiente pasa a listado de completadas
    public Tarea completarTarea(int id) 
    
    throws TareaNoEncontradaException {

        //validacion
        Tarea tareap = buscarEnLista(tareas_pendientes, id);
        if (tareap == null) {
            throw new TareaNoEncontradaException("No existe esa tarea como pendiente ");
        }

        //cambio de listado
        tareas_pendientes.remove(tareap);
        tareap.setCompletada(true);
        tareas_completadas.add(tareap);

        return tareap;
    }


    //Tarea completada pasa a listado de pendiente
    public Tarea reabrirTarea(int id) 
    
    throws TareaNoEncontradaException {
        
        //Validacion
        Tarea tareac = buscarEnLista(tareas_completadas, id);
        if (tareac == null) {
            throw new TareaNoEncontradaException("No existe esa tarea como completada");
        }

        //cambio de listado
        tareas_completadas.remove(tareac);
        tareac.setCompletada(false);
        tareas_pendientes.add(tareac);

        return tareac;
    }
    


    // ---------- Ordenamiento de listas pendientes y completadas ----------



    //fecha limite ascendente aplicando ordenamiento por insercion, con parametro para direccion
    private void ordenarInsercionPorFecha(List<Tarea> lista, boolean ascendente) {
        for (int i = 1; i < lista.size(); i++) {
            Tarea actual = lista.get(i);
            int j = i - 1;
 
            while (j >= 0) {
                boolean correr;

                //se basa en el paramtro de ordenamiento
                if (ascendente) {
                    // se corre solo si la fecha de la izquierda es posterior
                    correr = lista.get(j).getFechaLimite().isAfter(actual.getFechaLimite());
                } else {
                    // se corre solo si la fecha de la izquierda es anterior
                    correr = lista.get(j).getFechaLimite().isBefore(actual.getFechaLimite());
                }
 
                if (!correr) { //si son fechas iguales se detiene
                    break; 
                }
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
    }
    
    //Ordenamiento por fecha, recibe lista y valor de ordenamiento
   public List<Tarea> ordenarPorFecha(boolean pendientes, boolean ascendente) {
 
        // Se ordena una copia, asi las listas originales no cambian
        List<Tarea> copia = new ArrayList<>(pendientes ? tareas_pendientes : tareas_completadas);
        ordenarInsercionPorFecha(copia, ascendente);
        return copia;
    }


    //Ordenar por prioridad de tarea, recibe lista y valor de ordenamiento
    public List<Tarea> ordenarPorPrioridad(boolean pendientes, boolean ascendente) {
 
        List<Tarea> copia = new ArrayList<>(pendientes ? tareas_pendientes : tareas_completadas);
        ordenarBurbujaPorPrioridad(copia, ascendente);
        return copia;
    }

    //Ordenamiento burbuja, con prioridad descendente 
    private void ordenarBurbujaPorPrioridad(List<Tarea> lista, boolean ascendente) {
        int n = lista.size();
        for (int i = 0; i < n - 1; i++) {
            boolean huboIntercambio = false;
 
            for (int j = 0; j < n - 1 - i; j++) {
                boolean intercambiar;

                //se basa en el paramtro de ordenamiento
                if (ascendente) {
                    // Si la de la izquierda tiene mayor prioridad se intercambia
                    intercambiar = lista.get(j).getPrioridad() > lista.get(j + 1).getPrioridad();
                } else {
                    // Si la de la izquierda tiene menor prioridad se intercambia
                    intercambiar = lista.get(j).getPrioridad() < lista.get(j + 1).getPrioridad();
                }
 
                // con prioridades iguales no se intercambia
                if (intercambiar) {
                    Tarea temp = lista.get(j);
                    lista.set(j, lista.get(j + 1));
                    lista.set(j + 1, temp);
                    huboIntercambio = true;
                }
            }
            if (!huboIntercambio) {
                break;
            }
        }
    }

        //Vuelve a agregar una tarea eliminada a su listado
    public void restaurarTarea(Tarea tarea) {
        if (tarea == null) return;
        if (tarea.isCompletada()) {
            tareas_completadas.add(tarea);
        } else {
            tareas_pendientes.add(tarea);
        }
    }

    
}