import excepciones.CampoVacioException;
import excepciones.FechaInvalidaException;
import excepciones.PrioridadInvalidaException;
import excepciones.TareaNoEncontradaException;
import modelo.servicios.GestorTareas;
import modelo.entidades.Tarea;
 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 
/**
 * Pruebas de consola del GestorTareas.
 * Cada comprobacion imprime [OK] o [FALLO]; al final se muestra el resumen.
 * Las fechas se calculan a partir de hoy, asi que la prueba no caduca.
 */
public class PruebaGestor_BorrarDespues {
 
    private static int aprobadas = 0;
    private static int falladas = 0;
 
    // Para poder pasar codigo que lanza excepciones a esperarExcepcion
    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws Exception;
    }
 
    public static void main(String[] args) throws Exception {
        final GestorTareas g = new GestorTareas();
 
        // ------------------------------------------------------------
        titulo("1. CREAR TAREAS");
        // Orden de creacion (ids 1 a 5). T2 y T4 empatan en fecha y prioridad
        // a proposito, para comprobar que los ordenamientos son estables.
        //   id nombre      prioridad  fecha
        //   1  Estudiar    3          hoy+10
        //   2  Proyecto    5          hoy+3
        //   3  Comprar     1          hoy+7
        //   4  Informe     5          hoy+3
        //   5  Gym         3          hoy+1
        g.crearTarea("Estudiar", "Parcial", 3, enDias(10), "Estudio");
        g.crearTarea("Proyecto", "Java", 5, enDias(3), "Trabajo");
        g.crearTarea("Comprar", "Super", 1, enDias(7), "Casa");
        g.crearTarea("Informe", "Reporte", 5, enDias(3), "Trabajo");
        g.crearTarea("Gym", "Piernas", 3, enDias(1), "Salud");
 
        verificar("Hay 5 pendientes", g.getPendientes().size() == 5);
        verificar("Hay 0 completadas", g.getCompletadas().size() == 0);
        verificarIds("Ids consecutivos 1..5", g.getPendientes(), 1, 2, 3, 4, 5);
        System.out.println("Pendientes creadas:");
        g.getPendientes().forEach(System.out::println);
 
        // ------------------------------------------------------------
        titulo("2. ORDENAR PENDIENTES POR FECHA (inserción)");
        verificarIds("Fecha ascendente (la más próxima primero)",
                g.ordenarPorFecha(true, true), 5, 2, 4, 3, 1);
        verificarIds("Fecha descendente (la más lejana primero)",
                g.ordenarPorFecha(true, false), 1, 3, 2, 4, 5);
        System.out.println("Fecha ascendente:");
        g.ordenarPorFecha(true, true).forEach(System.out::println);
 
        // ------------------------------------------------------------
        titulo("3. ORDENAR PENDIENTES POR PRIORIDAD (burbuja)");
        verificarIds("Prioridad ascendente (1 primero)",
                g.ordenarPorPrioridad(true, true), 3, 1, 5, 2, 4);
        verificarIds("Prioridad descendente (5 primero)",
                g.ordenarPorPrioridad(true, false), 2, 4, 1, 5, 3);
        System.out.println("Prioridad descendente:");
        g.ordenarPorPrioridad(true, false).forEach(System.out::println);
 
        // ------------------------------------------------------------
        titulo("4. LAS LISTAS ORIGINALES NO SE ALTERAN AL ORDENAR");
        verificarIds("Pendientes siguen en orden de creación",
                g.getPendientes(), 1, 2, 3, 4, 5);
 
        // ------------------------------------------------------------
        titulo("5. COMPLETAR TAREAS");
        Tarea completada = g.completarTarea(2);
        g.completarTarea(5);
        verificar("La tarea devuelta quedó marcada como completada", completada.isCompletada());
        verificar("Quedan 3 pendientes", g.getPendientes().size() == 3);
        verificar("Hay 2 completadas", g.getCompletadas().size() == 2);
        verificarIds("Completadas en orden en que se completaron", g.getCompletadas(), 2, 5);
        verificarIds("Pendientes restantes", g.getPendientes(), 1, 3, 4);
 
        // ------------------------------------------------------------
        titulo("6. ORDENAR LISTA DE COMPLETADAS");
        verificarIds("Completadas por fecha ascendente",
                g.ordenarPorFecha(false, true), 5, 2);
        verificarIds("Completadas por fecha descendente",
                g.ordenarPorFecha(false, false), 2, 5);
        verificarIds("Completadas por prioridad descendente",
                g.ordenarPorPrioridad(false, false), 2, 5);
        verificarIds("Completadas por prioridad ascendente",
                g.ordenarPorPrioridad(false, true), 5, 2);
 
        // ------------------------------------------------------------
        titulo("7. REABRIR (revertir un completar)");
        Tarea reabierta = g.reabrirTarea(2);
        verificar("La tarea reabierta ya no está completada", !reabierta.isCompletada());
        verificar("Pendientes: 4", g.getPendientes().size() == 4);
        verificar("Completadas: 1", g.getCompletadas().size() == 1);
        verificarIds("Completadas ahora solo tiene la 5", g.getCompletadas(), 5);
 
        // ------------------------------------------------------------
        titulo("8. ELIMINAR");
        Tarea eliminadaPendiente = g.eliminarTarea(1);
        verificar("Devuelve la tarea eliminada (id 1)", eliminadaPendiente.getId() == 1);
        verificar("Pendientes: 3", g.getPendientes().size() == 3);
        Tarea eliminadaCompletada = g.eliminarTarea(5);
        verificar("Se puede eliminar una completada (id 5)", eliminadaCompletada.getId() == 5);
        verificar("Completadas: 0", g.getCompletadas().size() == 0);
 
        // ------------------------------------------------------------
        titulo("9. ERRORES AL CREAR (deben lanzar la excepción correcta)");
        esperarExcepcion("Prioridad 0", PrioridadInvalidaException.class,
                () -> g.crearTarea("X", "", 0, enDias(5), "A"));
        esperarExcepcion("Prioridad 6", PrioridadInvalidaException.class,
                () -> g.crearTarea("X", "", 6, enDias(5), "A"));
        esperarExcepcion("Prioridad negativa", PrioridadInvalidaException.class,
                () -> g.crearTarea("X", "", -3, enDias(5), "A"));
        esperarExcepcion("Fecha con formato malo", FechaInvalidaException.class,
                () -> g.crearTarea("X", "", 3, "31-12-2030", "A"));
        esperarExcepcion("Fecha con letras", FechaInvalidaException.class,
                () -> g.crearTarea("X", "", 3, "hoy mismo", "A"));
        esperarExcepcion("Fecha inexistente (30 de febrero)", FechaInvalidaException.class,
                () -> g.crearTarea("X", "", 3, "2030-02-30", "A"));
        esperarExcepcion("Fecha en el pasado", FechaInvalidaException.class,
                () -> g.crearTarea("X", "", 3, enDias(-1), "A"));
        esperarExcepcion("Fecha vacía", FechaInvalidaException.class,
                () -> g.crearTarea("X", "", 3, "", "A"));
        esperarExcepcion("Fecha nula", FechaInvalidaException.class,
                () -> g.crearTarea("X", "", 3, null, "A"));
        esperarExcepcion("Nombre vacío", CampoVacioException.class,
                () -> g.crearTarea("", "", 3, enDias(5), "A"));
        esperarExcepcion("Nombre solo espacios", CampoVacioException.class,
                () -> g.crearTarea("   ", "", 3, enDias(5), "A"));
        esperarExcepcion("Nombre nulo", CampoVacioException.class,
                () -> g.crearTarea(null, "", 3, enDias(5), "A"));
        verificar("Los errores no agregaron tareas (siguen 3 pendientes)",
                g.getPendientes().size() == 3);
 
        // ------------------------------------------------------------
        titulo("10. ERRORES AL ELIMINAR / COMPLETAR / REABRIR");
        esperarExcepcion("Eliminar id inexistente", TareaNoEncontradaException.class,
                () -> g.eliminarTarea(999));
        esperarExcepcion("Eliminar una tarea ya eliminada (id 1)", TareaNoEncontradaException.class,
                () -> g.eliminarTarea(1));
        esperarExcepcion("Completar id inexistente", TareaNoEncontradaException.class,
                () -> g.completarTarea(999));
        esperarExcepcion("Completar una tarea que no está pendiente (id 5, eliminada)",
                TareaNoEncontradaException.class, () -> g.completarTarea(5));
        esperarExcepcion("Reabrir una tarea que no está completada (id 3, pendiente)",
                TareaNoEncontradaException.class, () -> g.reabrirTarea(3));
        esperarExcepcion("Reabrir id inexistente", TareaNoEncontradaException.class,
                () -> g.reabrirTarea(999));
 
        // ------------------------------------------------------------
        titulo("11. LOS ERRORES NO GASTAN IDS");
        Tarea nueva = g.crearTarea("Nueva", "Despues de errores", 2, enDias(4), "Casa");
        verificar("La nueva tarea recibe el id 6 (no se saltó ningún número)", nueva.getId() == 6);
 
        // ------------------------------------------------------------
        titulo("12. LISTAS VACÍAS (no deben fallar)");
        GestorTareas vacio = new GestorTareas();
        verificar("Ordenar fecha de lista vacía devuelve lista vacía",
                vacio.ordenarPorFecha(true, true).isEmpty());
        verificar("Ordenar prioridad de lista vacía devuelve lista vacía",
                vacio.ordenarPorPrioridad(false, false).isEmpty());
 
        GestorTareas unica = new GestorTareas();
        unica.crearTarea("Unica", "", 4, enDias(2), "A");
        verificarIds("Ordenar una sola tarea la deja igual",
                unica.ordenarPorPrioridad(true, true), 1);
 
        // ------------------------------------------------------------
        System.out.println();
        System.out.println("=======================================");
        System.out.println("RESUMEN: " + aprobadas + " correctas, " + falladas + " fallidas");
        System.out.println(falladas == 0 ? "TODO EN ORDEN" : "HAY PRUEBAS FALLIDAS, REVISAR");
        System.out.println("=======================================");
    }
 
    // ---------------- Utilidades de prueba ----------------
 
    private static String enDias(int dias) {
        return LocalDate.now().plusDays(dias).toString();
    }
 
    private static void titulo(String texto) {
        System.out.println();
        System.out.println("---- " + texto + " ----");
    }
 
    private static void verificar(String caso, boolean condicion) {
        if (condicion) {
            aprobadas++;
            System.out.println("[OK]    " + caso);
        } else {
            falladas++;
            System.out.println("[FALLO] " + caso);
        }
    }
 
    // Compara los ids de la lista obtenida con los esperados, en el mismo orden
    private static void verificarIds(String caso, List<Tarea> obtenida, Integer... esperados) {
        List<Integer> ids = new ArrayList<>();
        for (Tarea t : obtenida) {
            ids.add(t.getId());
        }
        List<Integer> esperadosLista = Arrays.asList(esperados);
        if (ids.equals(esperadosLista)) {
            aprobadas++;
            System.out.println("[OK]    " + caso + " -> " + ids);
        } else {
            falladas++;
            System.out.println("[FALLO] " + caso + " -> obtuvo " + ids + ", esperaba " + esperadosLista);
        }
    }
 
    // Comprueba que la operacion lance exactamente el tipo de excepcion esperado
    private static void esperarExcepcion(String caso, Class<? extends Exception> tipo, Operacion op) {
        try {
            op.ejecutar();
            falladas++;
            System.out.println("[FALLO] " + caso + " -> no lanzó ninguna excepción");
        } catch (Exception e) {
            if (tipo.isInstance(e)) {
                aprobadas++;
                System.out.println("[OK]    " + caso + " -> " + e.getMessage());
            } else {
                falladas++;
                System.out.println("[FALLO] " + caso + " -> lanzó " + e.getClass().getSimpleName()
                        + " en lugar de " + tipo.getSimpleName());
            }
        }
    }
}