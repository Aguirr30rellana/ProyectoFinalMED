package controlador;

import excepciones.CampoVacioException;
import excepciones.CategoriaException;
import excepciones.CategoriaNoEncontradaException;
import excepciones.ColaVaciaException;
import excepciones.FechaInvalidaException;
import excepciones.HistorialVacioException;
import excepciones.PrioridadInvalidaException;
import excepciones.TareaNoEncontradaException;
import modelo.entidades.Accion;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;
import modelo.entidades.TareaEnEspera;
import modelo.servicios.ArbolCategorias;
import modelo.servicios.ColaEspera;
import modelo.servicios.GestorTareas;
import modelo.servicios.PilaHistorial;
import utils.BuscadorBinario;
import utils.ConvertorFecha;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ControladorTareas {

    // Opciones que muestra la ventana en sus combos
    public enum Filtro { TODAS, PENDIENTES, COMPLETADAS }
    public enum Orden  { NINGUNO, FECHA_ASC, FECHA_DESC, PRIORIDAD_ASC, PRIORIDAD_DESC }

    private final ArbolCategorias arbol;
    private final GestorTareas gestor;
    private final PilaHistorial historial;
    private final ColaEspera cola;

    public ControladorTareas(ArbolCategorias arbol, GestorTareas gestor, PilaHistorial historial,
                                ColaEspera cola) {
        this.arbol = arbol;
        this.gestor = gestor;
        this.historial = historial;
        this.cola = cola;
    }

    // Crear / editar / eliminar

    public Tarea crearTarea(NodoCategoria categoria, String nombre, String descripcion, int prioridad, String fecha)
            throws CategoriaException, FechaInvalidaException, PrioridadInvalidaException, CampoVacioException {
        if (categoria == null) {
            throw new CategoriaException("Seleccione la categoria de la tarea.");
        }
        Tarea nueva = gestor.crearTarea(nombre, descripcion, prioridad, fecha, categoria.obtenerRutaCompleta());
        categoria.agregarTarea(nueva);
        historial.registrarAccion(new Accion(Accion.TipoAccion.Agregar, nueva));
        return nueva;
    }

    public void editarTarea(Tarea tarea, NodoCategoria nuevaCategoria, String nombre, String descripcion,
                            int prioridad, String fechaTexto)
            throws TareaNoEncontradaException, CategoriaException, FechaInvalidaException,
                    PrioridadInvalidaException, CampoVacioException {
        if (tarea == null) {
            throw new TareaNoEncontradaException("Seleccione la tarea que desea editar.");
        }
        if (nuevaCategoria == null) {
            throw new CategoriaException("Seleccione la categoria de la tarea.");
        }

        // Se valida todo ANTES de cambiar algo, para no dejar la tarea a medias
        LocalDate fecha = ConvertorFecha.parsearFecha(fechaTexto);
        if (fecha.isBefore(LocalDate.now()) && !fecha.equals(tarea.getFechaLimite())) {
            throw new FechaInvalidaException("La nueva fecha limite no puede ser una fecha pasada.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CampoVacioException("nombre");
        }
        if (prioridad < Tarea.PRIORIDAD_MIN || prioridad > Tarea.PRIORIDAD_MAX) {
            throw new PrioridadInvalidaException("La prioridad debe estar entre "
                    + Tarea.PRIORIDAD_MIN + " y " + Tarea.PRIORIDAD_MAX + ".");
        }

        tarea.setNombre(nombre);
        tarea.setDescripcion(descripcion);
        tarea.setPrioridad(prioridad);
        tarea.setFechaLimite(fecha);

        // Si cambio de categoria, se mueve de un nodo a otro
        NodoCategoria actual = categoriaDe(tarea);
        if (actual != nuevaCategoria) {
            if (actual != null) actual.eliminarTarea(tarea);
            nuevaCategoria.agregarTarea(tarea);
            tarea.setCategoria(nuevaCategoria.obtenerRutaCompleta());
        }
    }

    public void eliminarTarea(Tarea tarea) throws TareaNoEncontradaException {
        if (tarea == null) {
            throw new TareaNoEncontradaException("Seleccione la tarea que desea eliminar.");
        }
        gestor.eliminarTarea(tarea.getId());
        NodoCategoria nodo = categoriaDe(tarea);
        if (nodo != null) nodo.eliminarTarea(tarea);
        historial.registrarAccion(new Accion(Accion.TipoAccion.Eliminar, tarea));
    }

    // Completar / reabrir 

    public void completarTarea(Tarea tarea) throws TareaNoEncontradaException {
        if (tarea == null) {
            throw new TareaNoEncontradaException("Seleccione la tarea que desea completar.");
        }
        gestor.completarTarea(tarea.getId());
        historial.registrarAccion(new Accion(Accion.TipoAccion.Completar, tarea));
    }

    public void reabrirTarea(Tarea tarea) throws TareaNoEncontradaException {
        if (tarea == null) {
            throw new TareaNoEncontradaException("Seleccione la tarea que desea reabrir.");
        }
        gestor.reabrirTarea(tarea.getId());
        historial.registrarAccion(new Accion(Accion.TipoAccion.Reabrir, tarea));
    }

    // Deshacer (pila) 

    // Saca la ultima accion de la pila y hace lo contrario
    public String deshacer() throws HistorialVacioException, TareaNoEncontradaException,
                                    CategoriaNoEncontradaException {
        Accion accion = historial.deshacerUltimaAccion();
        if (accion == null) {
            throw new HistorialVacioException("No hay acciones para deshacer.");
        }
        Tarea tarea = accion.getTarea();

        switch (accion.getTipo()) {
            case Agregar:                                  
                gestor.eliminarTarea(tarea.getId());
                NodoCategoria nodo = categoriaDe(tarea);
                if (nodo != null) nodo.eliminarTarea(tarea);
                return "Se deshizo la creacion de \"" + tarea.getNombre() + "\".";

            case Eliminar:                                  // se habia eliminado -> se restaura
                NodoCategoria destino = buscarNodoPorRuta(arbol.getRaiz(), tarea.getCategoria());
                if (destino == null) {
                    throw new CategoriaNoEncontradaException("La categoria de \"" + tarea.getNombre()
                            + "\" ya no existe, no se puede restaurar.");
                }
                gestor.restaurarTarea(tarea);
                destino.agregarTarea(tarea);
                return "Se restauro la tarea \"" + tarea.getNombre() + "\".";

            case Completar:
                gestor.reabrirTarea(tarea.getId());
                return "\"" + tarea.getNombre() + "\" volvió a estar pendiente.";

            case Reabrir:
                gestor.completarTarea(tarea.getId());
                return "\"" + tarea.getNombre() + "\" volvió a estar completada.";
            default:
                return "";
        }
    }

    public int accionesEnHistorial() { return historial.obtenerCantidad(); }

    // Cola de espera

    // Recibe una tarea sin categoria y la pone al final de la cola
    public TareaEnEspera recibirTarea(String nombre, String descripcion, int prioridad, String fecha)
            throws CampoVacioException, PrioridadInvalidaException, FechaInvalidaException {
        TareaEnEspera recibida = new TareaEnEspera(nombre, descripcion, prioridad, fecha);
        cola.encolar(recibida);
        return recibida;
    }

    public Tarea asignarSiguiente(NodoCategoria categoria)
            throws ColaVaciaException, CategoriaException, FechaInvalidaException,
                    PrioridadInvalidaException, CampoVacioException {
        TareaEnEspera siguiente = cola.verSiguiente();
        if (siguiente == null) {
            throw new ColaVaciaException("No hay tareas en espera de ser asignadas.");
        }
        Tarea creada = crearTarea(categoria, siguiente.getNombre(), siguiente.getDescripcion(),
                siguiente.getPrioridad(), siguiente.getFechaLimite().toString());
        cola.atenderSiguiente();
        return creada;
    }

    // Saca la tarea del frente sin asignarla 
    public TareaEnEspera descartarSiguiente() throws ColaVaciaException {
        TareaEnEspera descartada = cola.atenderSiguiente();
        if (descartada == null) {
            throw new ColaVaciaException("No hay tareas en espera.");
        }
        return descartada;
    }

        // Consulta la tarea del frente sin sacarla de la cola
    public TareaEnEspera verSiguiente() throws ColaVaciaException {
        TareaEnEspera siguiente = cola.verSiguiente();
        if (siguiente == null) {
            throw new ColaVaciaException("No hay tareas en espera de ser asignadas.");
        }
        return siguiente;
    }

    public List<TareaEnEspera> tareasEnEspera() { return cola.obtenerTodas(); }
    public int totalEnEspera()                  { return cola.obtenerCantidad(); }

    // Consultas para la lista

    public List<Tarea> tareasParaMostrar(NodoCategoria nodo, Filtro filtro, Orden orden) {
        List<Tarea> rama = arbol.obtenerTareasDeRama(nodo);
        List<Tarea> base = new ArrayList<>();

        if (filtro != Filtro.COMPLETADAS) base.addAll(listaOrdenada(true, orden));   
        if (filtro != Filtro.PENDIENTES)  base.addAll(listaOrdenada(false, orden));

        List<Tarea> resultado = new ArrayList<>();
        for (Tarea t : base) {
            if (rama.contains(t)) resultado.add(t);
        }
        return resultado;
    }

    private List<Tarea> listaOrdenada(boolean pendientes, Orden orden) {
        switch (orden) {
            case FECHA_ASC:      return gestor.ordenarPorFecha(pendientes, true);
            case FECHA_DESC:     return gestor.ordenarPorFecha(pendientes, false);
            case PRIORIDAD_ASC:  return gestor.ordenarPorPrioridad(pendientes, true);
            case PRIORIDAD_DESC: return gestor.ordenarPorPrioridad(pendientes, false);
            default:             return pendientes ? gestor.getPendientes() : gestor.getCompletadas();
        }
    }

    // Busqueda binaria por nombre

    public Tarea buscarPorNombre(String nombre) throws TareaNoEncontradaException, CampoVacioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CampoVacioException("nombre de la tarea a buscar");
        }
        // Copia de todas las tareas ordenada alfabeticamente 
        List<Tarea> copia = new ArrayList<>(gestor.getPendientes());
        copia.addAll(gestor.getCompletadas());
        ordenarPorNombre(copia);

        Tarea encontrada = BuscadorBinario.buscarPorNombre(copia, nombre.trim());
        if (encontrada == null) {
            throw new TareaNoEncontradaException("No existe una tarea llamada \"" + nombre.trim() + "\".");
        }
        return encontrada;
    }

    // Ordenamiento por insercion alfabetico (sin importar mayusculas)
    private void ordenarPorNombre(List<Tarea> lista) {
        for (int i = 1; i < lista.size(); i++) {
            Tarea actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && lista.get(j).getNombre().compareToIgnoreCase(actual.getNombre()) > 0) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
    }

    // Ubicar tareas en el arbol

    public NodoCategoria categoriaDe(Tarea tarea) {
        return buscarNodoDeTarea(arbol.getRaiz(), tarea);
    }

    private NodoCategoria buscarNodoDeTarea(NodoCategoria nodo, Tarea tarea) {
        if (nodo.getTareas().contains(tarea)) return nodo;
        for (NodoCategoria hijo : nodo.getHijos()) {
            NodoCategoria encontrado = buscarNodoDeTarea(hijo, tarea);
            if (encontrado != null) return encontrado;
        }
        return null;
    }

    private NodoCategoria buscarNodoPorRuta(NodoCategoria nodo, String ruta) {
        if (nodo.obtenerRutaCompleta().equalsIgnoreCase(ruta)) return nodo;
        for (NodoCategoria hijo : nodo.getHijos()) {
            NodoCategoria encontrado = buscarNodoPorRuta(hijo, ruta);
            if (encontrado != null) return encontrado;
        }
        return null;
    }


    // Todas las categorias (para el combo del formulario de tareas)
    public List<NodoCategoria> listarCategorias() { return arbol.listarCategorias(); }

    public int totalPendientes()  { return gestor.getPendientes().size(); }
    public int totalCompletadas() { return gestor.getCompletadas().size(); }
}