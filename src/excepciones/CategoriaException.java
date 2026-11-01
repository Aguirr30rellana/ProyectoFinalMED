package excepciones;

// Errores del arbol de categorias: duplicada, no encontrada, eliminar raiz, rama con tareas
public class CategoriaException extends Exception {
    public CategoriaException(String mensaje) {
        super(mensaje);
    }
} 