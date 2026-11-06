package modelo.entidades;
 
import excepciones.CampoVacioException;
import excepciones.FechaInvalidaException;
import excepciones.PrioridadInvalidaException;
import java.time.LocalDate;
import java.io.Serializable; 

public class Tarea implements Serializable { 
    private static final long serialVersionUID = 1L; 
    
    public static final int PRIORIDAD_MIN = 1;
    public static final int PRIORIDAD_MAX = 5;
    
    //atributos
    private final int id;
    private String nombre;
    private String descripcion;
    private int prioridad;
    private LocalDate fechaLimite;
    private String categoria;
    private boolean completada;
    
    //constructor
    public Tarea(int id, String nombre, String descripcion, int prioridad, LocalDate fechaLimite, String categoria)

        //excepciones declaradas en el constructor
        throws PrioridadInvalidaException, FechaInvalidaException, CampoVacioException {
        this.id = id;
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrioridad(prioridad);
        setFechaLimite(fechaLimite);
        setCategoria(categoria);
        this.completada = false;
    }
    

    //Consultar estados, getters
    public int getId() { return id; }

    public String getNombre() { return nombre; }

    public String getDescripcion() { return descripcion; }

    public int getPrioridad() { return prioridad; }

    public LocalDate getFechaLimite() { return fechaLimite; }

    public String getCategoria() { return categoria; }

    public boolean isCompletada() { return completada; }
    

    //Cambiar datos, setters ---------------------------------------------

    public void setNombre(String nombre)  throws CampoVacioException {
        //manejo de exepcion, campo vacio
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CampoVacioException("nombre");
        }
        this.nombre = nombre.trim();
    }

    public void setDescripcion(String descripcion) { 
        this.descripcion = (descripcion == null) ? "" : descripcion.trim();
    }

    public void setCategoria(String categoria) throws CampoVacioException {

        //manejo de exepcion, campo vacio
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new CampoVacioException("categoria");
        }
        this.categoria = categoria.trim(); 
    }

    public void setCompletada(boolean completada) { this.completada = completada; }
 
    public void setPrioridad(int prioridad) throws PrioridadInvalidaException {

        //manejo de exepcion, Prioridad en un rango valido
        if (prioridad < PRIORIDAD_MIN || prioridad > PRIORIDAD_MAX) {
            throw new PrioridadInvalidaException("La prioridad debe estar en un rango permitivo");
        }
        this.prioridad = prioridad;
    }
 
    public void setFechaLimite(LocalDate fechaLimite) throws FechaInvalidaException {

        //manejo de exepcion, fecha nula
        if (fechaLimite == null) {
            throw new FechaInvalidaException("La fecha limite no puede ser nula.");
        }
        this.fechaLimite = fechaLimite;
    }

}