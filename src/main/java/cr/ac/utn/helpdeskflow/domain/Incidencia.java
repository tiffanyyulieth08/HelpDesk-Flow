package cr.ac.utn.helpdeskflow.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;

/** Representa una incidencia y controla sus reglas de negocio principales. */
public class Incidencia {

    private final UUID id;
    private final String titulo;
    private final String descripcion;
    private final String categoria;
    private final Impacto impacto;
    private final Urgencia urgencia;
    private final LocalDateTime fechaCreacion;
    private ClaseServicio claseServicio;
    private EstadoIncidencia estado;
    private String solucionAplicada;
    private LocalDateTime fechaCierre;

    private Incidencia(String titulo, String descripcion, String categoria, Impacto impacto, Urgencia urgencia) {
        validarDatosObligatorios(titulo, descripcion, categoria, impacto, urgencia);
        this.id = UUID.randomUUID();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.impacto = impacto;
        this.urgencia = urgencia;
        this.fechaCreacion = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        this.claseServicio = ClaseServicio.NORMAL;
        this.estado = EstadoIncidencia.REGISTRADA;
    }

    private Incidencia(UUID id, String titulo, String descripcion, String categoria,
                       Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                       String solucionAplicada, LocalDateTime fechaCreacion,
                       LocalDateTime fechaCierre,
                       ClaseServicio claseServicio) {
        validarDatosObligatorios(titulo, descripcion, categoria, impacto, urgencia);
        if (id == null || estado == null || fechaCreacion == null || claseServicio == null) {
            throw new ReglaNegocioException("Los datos de persistencia obligatorios no pueden ser nulos");
        }
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.impacto = impacto;
        this.urgencia = urgencia;
        this.fechaCreacion = fechaCreacion;
        this.claseServicio = claseServicio;
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

    private static void validarDatosObligatorios(String titulo, String descripcion,
                                                  String categoria, Impacto impacto,
                                                  Urgencia urgencia) {
        validarTitulo(titulo);
        validarDescripcion(descripcion);
        if (categoria == null || categoria.isBlank()) {
            throw new ReglaNegocioException("La categoria es obligatoria");
        }
        if (impacto == null) {
            throw new ReglaNegocioException("El impacto es obligatorio");
        }
        if (urgencia == null) {
            throw new ReglaNegocioException("La urgencia es obligatoria");
        }
    }

    /** Crea una incidencia nueva en estado {@link EstadoIncidencia#REGISTRADA}. */
    public static Incidencia crear(String titulo, String descripcion, String categoria, Impacto impacto, Urgencia urgencia) {
        return new Incidencia(titulo, descripcion, categoria, impacto, urgencia);
    }

    /** Rehidrata una incidencia antigua usando valores compatibles con el modelo inicial. */
    public static Incidencia rehidratar(UUID id, String titulo, String descripcion, String categoria,
                                        Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                                        String solucionAplicada, LocalDateTime fechaCierre) {
        return new Incidencia(id, titulo, descripcion, categoria, impacto, urgencia,
                              estado, solucionAplicada, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                              fechaCierre, ClaseServicio.NORMAL);
    }

    /** Rehidrata una incidencia persistida sin fecha de creación explícita. */
    public static Incidencia rehidratar(UUID id, String titulo, String descripcion, String categoria,
                                        Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                                        String solucionAplicada, LocalDateTime fechaCierre,
                                        ClaseServicio claseServicio) {
        return new Incidencia(id, titulo, descripcion, categoria, impacto, urgencia,
                              estado, solucionAplicada, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                              fechaCierre, claseServicio);
    }

    /** Rehidrata una incidencia con todos sus datos persistidos. */
    public static Incidencia rehidratar(UUID id, String titulo, String descripcion, String categoria,
                                        Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                                        String solucionAplicada, LocalDateTime fechaCreacion,
                                        LocalDateTime fechaCierre, ClaseServicio claseServicio) {
        return new Incidencia(id, titulo, descripcion, categoria, impacto, urgencia,
                              estado, solucionAplicada, fechaCreacion, fechaCierre, claseServicio);
    }

    /** Devuelve el identificador único de la incidencia. */
    public UUID getId() {
        return id;
    }

    /** Devuelve el título de la incidencia. */
    public String getTitulo() {
        return titulo;
    }

    /** Devuelve la descripción reportada. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Devuelve la categoría funcional. */
    public String getCategoria() {
        return categoria;
    }

    /** Devuelve el impacto asignado. */
    public Impacto getImpacto() {
        return impacto;
    }

    /** Devuelve la urgencia asignada. */
    public Urgencia getUrgencia() {
        return urgencia;
    }

    /** Calcula la prioridad a partir del impacto y la urgencia actuales. */
    public Prioridad getPrioridad() {
        return CalculadoraPrioridad.calcular(impacto, urgencia);
    }

    /** Devuelve la clase de servicio vigente. */
    public ClaseServicio getClaseServicio() {
        return claseServicio;
    }

    /** Marca la incidencia como EXPEDITE si su prioridad es crítica. */
    public void marcarComoExpedite() {
        if (getPrioridad() != Prioridad.CRITICA) {
            throw new ReglaNegocioException("Solo las incidencias criticas pueden ser EXPEDITE");
        }
        claseServicio = ClaseServicio.EXPEDITE;
    }

    /** Devuelve el estado actual del flujo. */
    public EstadoIncidencia getEstado() {
        return estado;
    }

    /** Avanza la incidencia al siguiente estado permitido del flujo. */
    public void cambiarEstado(EstadoIncidencia nuevoEstado) {
        ValidadorTransicion.validar(estado, nuevoEstado, solucionAplicada);
        estado = nuevoEstado;
        if (estado == EstadoIncidencia.FINALIZADA) {
            fechaCierre = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    /** Registra la solución que permitirá finalizar la incidencia. */
    public void registrarSolucion(String solucion) {
        solucionAplicada = solucion;
    }

    /** Devuelve la solución registrada, si existe. */
    public String getSolucionAplicada() {
        return solucionAplicada;
    }

    /** Devuelve la fecha de cierre o {@code null} si sigue abierta. */
    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    /** Devuelve la fecha de creación. */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
