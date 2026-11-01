import modelo.servicios.ArbolCategorias;
import modelo.servicios.GestorTareas;
import vista.MenuPrincipal;
import vista.estilos.Tema;

import javax.swing.*;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        // Datos compartidos por toda la aplicacion
        GestorTareas gestor = new GestorTareas();
        ArbolCategorias arbol = new ArbolCategorias("Categorias");
        cargarDatosDeEjemplo(gestor, arbol);

        // Swing se crea en el hilo de eventos (EDT)
        SwingUtilities.invokeLater(() -> {
            Tema.aplicar();
            new MenuPrincipal(gestor, arbol).setVisible(true);
        });
    }

    // Datos de prueba (borrar o cambiar cuando ya no se necesiten)
    private static void cargarDatosDeEjemplo(GestorTareas gestor, ArbolCategorias arbol) {
        try {
            arbol.agregarPorRuta("Trabajo", "Proyecto A", "Documentacion");
            arbol.agregarPorRuta("Trabajo", "Proyecto A", "Desarrollo");
            arbol.agregarPorRuta("Trabajo", "Reuniones");
            arbol.agregarPorRuta("Universidad", "Estructura de datos");
            arbol.agregarPorRuta("Personal", "Compras");

            gestor.crearTarea("Redactar manual", "Manual de usuario", 2, enDias(5), "Documentacion");
            gestor.crearTarea("Implementar login", "Pantalla de acceso", 1, enDias(3), "Desarrollo");
            gestor.crearTarea("Reunion semanal", "Avances del equipo", 3, enDias(1), "Reuniones");
            gestor.crearTarea("Entregar proyecto", "To-Do List avanzado", 1, enDias(10), "Estructura de datos");
            gestor.crearTarea("Comprar cuadernos", "", 4, enDias(2), "Compras");
            gestor.completarTarea(3);
        } catch (Exception e) {
            System.err.println("Error cargando datos de ejemplo: " + e.getMessage());
        }
    }

    private static String enDias(int dias) {
        return LocalDate.now().plusDays(dias).toString();
    }
}