package cr.ac.utn.helpdeskflow.domain;

import java.util.UUID;

import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;

public class Incidencia {

    private final UUID id;
    private final String titulo;
    private final String descripcion;
    private final String categoria;
    private final Impacto impacto;
    private final Urgencia urgencia;
    private final EstadoIncidencia estado;

    private Incidencia(String titulo, String descripcion, String categoria, Impacto impacto, Urgencia urgencia) {
        validarTitulo(titulo);
        validarDescripcion(descripcion);
        this.id = UUID.randomUUID();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.impacto = impacto;
        this.urgencia = urgencia;
        this.estado = EstadoIncidencia.REGISTRADA;
    }

    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new ReglaNegocioException("El titulo no puede ser nulo, vacio o solo espacios");
        }
    }

    private static void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.length() < 10) {
            throw new ReglaNegocioException("La descripcion debe tener al menos 10 caracteres");
        }
    }

    public static Incidencia crear(String titulo, String descripcion, String categoria, Impacto impacto, Urgencia urgencia) {
        return new Incidencia(titulo, descripcion, categoria, impacto, urgencia);
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public Impacto getImpacto() {
        return impacto;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public EstadoIncidencia getEstado() {
        return estado;
    }
}
