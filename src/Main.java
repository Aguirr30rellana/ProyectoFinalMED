import controlador.ControladorCategorias;
import controlador.ControladorTareas;
import excepciones.CampoVacioException;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;
import modelo.servicios.ArbolCategorias;
import modelo.servicios.ColaEspera;
import modelo.servicios.GestorTareas;
import modelo.servicios.PilaHistorial;
import utils.GestorArchivos;
import vista.VentanaPrincipal;
import vista.estilos.Tema;

import javax.swing.*;
import java.time.LocalDate;

public class Main {

    // Variables globales para guardar los datos
    public static GestorTareas gestorGlobal;
    public static ArbolCategorias arbolGlobal;

    public static void main(String[] args) {
        try {
            // 1. Intentar cargar los datos guardados
            Object[] datosCargados = GestorArchivos.cargarDatos();

            if (datosCargados != null) {
                gestorGlobal = (GestorTareas) datosCargados[0];
                arbolGlobal = (ArbolCategorias) datosCargados[1];
            } else {
                // Primera vez: iniciar de cero
                gestorGlobal = new GestorTareas();
                arbolGlobal = new ArbolCategorias("Categorias");
                cargarDatosDeEjemplo(gestorGlobal, arbolGlobal);
            }

            // Controladores
            ControladorCategorias ctrlCategorias = new ControladorCategorias(arbolGlobal);
            ControladorTareas ctrlTareas = new ControladorTareas(arbolGlobal, gestorGlobal, new PilaHistorial(), new ColaEspera());
            
            if (datosCargados == null) {
                cargarColaDeEjemplo(ctrlTareas); // Solo si es la primera vez
            }

            // Abrir la ventana PASÁNDOLE LOS DATOS (esto arregla el error)
            SwingUtilities.invokeLater(() -> {
                Tema.aplicar();
                new VentanaPrincipal(ctrlCategorias, ctrlTareas, gestorGlobal, arbolGlobal).setVisible(true);
            });
            
        } catch (CampoVacioException e) {
            System.err.println("Error fatal al inicializar el sistema: " + e.getMessage());
        }
    }

    private static void cargarDatosDeEjemplo(GestorTareas gestor, ArbolCategorias arbol) {
        try {
            NodoCategoria n1 = arbol.agregarPorRuta("Trabajo", "Proyecto A", "Documentacion");
            NodoCategoria n2 = arbol.agregarPorRuta("Trabajo", "Proyecto A", "Desarrollo");
            NodoCategoria n3 = arbol.agregarPorRuta("Trabajo", "Reuniones");
            NodoCategoria n4 = arbol.agregarPorRuta("Universidad", "Estructura de datos");
            NodoCategoria n5 = arbol.agregarPorRuta("Personal", "Compras");

            Tarea t1 = gestor.crearTarea("Redactar manual", "Manual de usuario", 2, enDias(5), n1.obtenerRutaCompleta());
            n1.agregarTarea(t1);
            Tarea t2 = gestor.crearTarea("Implementar login", "Pantalla de acceso", 1, enDias(3), n2.obtenerRutaCompleta());
            n2.agregarTarea(t2);
            Tarea t3 = gestor.crearTarea("Reunion semanal", "Avances del equipo", 3, enDias(1), n3.obtenerRutaCompleta());
            n3.agregarTarea(t3);
            Tarea t4 = gestor.crearTarea("Entregar proyecto", "To-Do List avanzado", 1, enDias(10), n4.obtenerRutaCompleta());
            n4.agregarTarea(t4);
            Tarea t5 = gestor.crearTarea("Comprar cuadernos", "", 4, enDias(2), n5.obtenerRutaCompleta());
            n5.agregarTarea(t5);
            gestor.completarTarea(3);
        } catch (Exception e) {
            System.err.println("Error cargando datos de ejemplo: " + e.getMessage());
        }
    }

    private static void cargarColaDeEjemplo(ControladorTareas ctrlTareas) {
        try {
            ctrlTareas.recibirTarea("Revisar correo del cliente", "Pedido urgente", 2, enDias(2));
            ctrlTareas.recibirTarea("Preparar presentacion", "", 3, enDias(6));
        } catch (Exception e) {
            System.err.println("Error cargando la cola de ejemplo: " + e.getMessage());
        }
    }

    private static String enDias(int dias) {
        return LocalDate.now().plusDays(dias).toString();
    }
}