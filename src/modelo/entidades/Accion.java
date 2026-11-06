package modelo.entidades;
import java.io.Serializable; 

public class Accion implements Serializable { 
    private static final long serialVersionUID = 1L; 

    public enum TipoAccion {Agregar,Eliminar,Completar, Reabrir}

    private TipoAccion tipo;
    private Tarea tarea;

    public Accion(TipoAccion tipo, Tarea tarea) {
        this.tipo = tipo;
        this.tarea = tarea;
    }

    public TipoAccion getTipo() {
        return tipo;
    }

    public Tarea getTarea() {
        return tarea;
    }
}