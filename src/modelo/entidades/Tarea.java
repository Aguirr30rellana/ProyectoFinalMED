package modelo.entidades;

import java.time.LocalDate;

public class Tarea implements Comparable<Tarea> {
    private int id;
    private String nombre;
    private String descripcion;
    private int prioridad;
    private LocalDate fechaLimite;
    private String categoria;
    private boolean completada;

    public Tarea() {
    }

    // Constructor 
    public Tarea(int id, String nombre, String descripcion, int prioridad, LocalDate fechaLimite, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.fechaLimite = fechaLimite;
        this.categoria = categoria;
        this.completada = false;
    }

    // Constructor 
    public Tarea(String nombre, int prioridad) {
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.completada = false;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    @Override
    public int compareTo(Tarea otra) {
        if (otra == null || otra.getNombre() == null) return 1;
        if (this.nombre == null) return -1;
        return this.nombre.compareToIgnoreCase(otra.getNombre());
    }

    @Override
    public String toString() {
        return nombre + " | Prioridad: " + prioridad + " | Estado: " + (completada ? "Completada" : "Pendiente");
    }
}