package utils;

import modelo.servicios.GestorTareas;
import modelo.servicios.ArbolCategorias;
import java.io.*;

public class GestorArchivos {
    private static final String RUTA_ARCHIVO = "datos_gestor.dat";

    public static void guardarDatos(GestorTareas gestor, ArbolCategorias arbol) {
        try (FileOutputStream fos = new FileOutputStream(RUTA_ARCHIVO);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            
            Object[] datos = {gestor, arbol};
            oos.writeObject(datos);
            System.out.println("💾 Datos guardados correctamente.");
            
        } catch (IOException e) {
            System.err.println("❌ Error al guardar los datos: " + e.getMessage());
        }
    }

    public static Object[] cargarDatos() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) return null; // Primera vez

        try (FileInputStream fis = new FileInputStream(archivo);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            Object[] datos = (Object[]) ois.readObject();
            System.out.println("📂 Datos cargados exitosamente.");
            return datos;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error al cargar los datos: " + e.getMessage());
            return null;
        }
    }
}