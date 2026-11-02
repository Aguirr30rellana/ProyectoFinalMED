package modelo.entidades;

public class Accion {

    public enum TipoAccion {Agregar,Eliminar,Completar}

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