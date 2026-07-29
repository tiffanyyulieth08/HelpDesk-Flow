package cr.ac.utn.helpdeskflow.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;

public class Incidencia {

    private final UUID id;
    private final String titulo;
    private final String descripcion;
    private final String categoria;
    private final Impacto impacto;
    private final Urgencia urgencia;
    private EstadoIncidencia estado;
    private String solucionAplicada;
    private LocalDateTime fechaCierre;

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

    private Incidencia(UUID id, String titulo, String descripcion, String categoria,
                       Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                       String solucionAplicada, LocalDateTime fechaCierre) {
        validarTitulo(titulo);
        validarDescripcion(descripcion);
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.impacto = impacto;
        this.urgencia = urgencia;
        this.estado = estado;
        this.solucionAplicada = solucionAplicada;
        this.fechaCierre = fechaCierre;
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

    public static Incidencia rehidratar(UUID id, String titulo, String descripcion, String categoria,
                                        Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                                        String solucionAplicada, LocalDateTime fechaCierre) {
        return new Incidencia(id, titulo, descripcion, categoria, impacto, urgencia,
                              estado, solucionAplicada, fechaCierre);
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

    public void cambiarEstado(EstadoIncidencia nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.ordinal() != estado.ordinal() + 1) {
            throw new ReglaNegocioException("Solo se permite avanzar al estado siguiente");
        }
        if (nuevoEstado == EstadoIncidencia.FINALIZADA
                && (solucionAplicada == null || solucionAplicada.isBlank())) {
            throw new ReglaNegocioException("No se puede finalizar sin una solucion registrada");
        }
        estado = nuevoEstado;
        if (estado == EstadoIncidencia.FINALIZADA) {
            fechaCierre = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public void registrarSolucion(String solucion) {
        solucionAplicada = solucion;
    }

    public String getSolucionAplicada() {
        return solucionAplicada;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }
}
