package excepciones;

// Errores del arbol de categorias: duplicada, no encontrada, eliminar raiz, rama con tareas
public class CategoriaException extends Exception {

    public CategoriaException() {
        super("ERROR, Existen fallos en la obtencion del arbol");
    }

    public CategoriaException(String mensaje) {
        super(mensaje);
    }
} 