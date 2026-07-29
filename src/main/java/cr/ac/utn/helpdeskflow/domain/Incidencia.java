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

    public static Incidencia crear(String titulo, String descripcion, String categoria, Impacto impacto, Urgencia urgencia) {
        return new Incidencia(titulo, descripcion, categoria, impacto, urgencia);
    }

    public static Incidencia rehidratar(UUID id, String titulo, String descripcion, String categoria,
                                        Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                                        String solucionAplicada, LocalDateTime fechaCierre) {
        return new Incidencia(id, titulo, descripcion, categoria, impacto, urgencia,
                              estado, solucionAplicada, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                              fechaCierre, ClaseServicio.NORMAL);
    }

    public static Incidencia rehidratar(UUID id, String titulo, String descripcion, String categoria,
                                        Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                                        String solucionAplicada, LocalDateTime fechaCierre,
                                        ClaseServicio claseServicio) {
        return new Incidencia(id, titulo, descripcion, categoria, impacto, urgencia,
                              estado, solucionAplicada, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                              fechaCierre, claseServicio);
    }

    public static Incidencia rehidratar(UUID id, String titulo, String descripcion, String categoria,
                                        Impacto impacto, Urgencia urgencia, EstadoIncidencia estado,
                                        String solucionAplicada, LocalDateTime fechaCreacion,
                                        LocalDateTime fechaCierre, ClaseServicio claseServicio) {
        return new Incidencia(id, titulo, descripcion, categoria, impacto, urgencia,
                              estado, solucionAplicada, fechaCreacion, fechaCierre, claseServicio);
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

    public Prioridad getPrioridad() {
        return CalculadoraPrioridad.calcular(impacto, urgencia);
    }

    public ClaseServicio getClaseServicio() {
        return claseServicio;
    }

    public void marcarComoExpedite() {
        if (getPrioridad() != Prioridad.CRITICA) {
            throw new ReglaNegocioException("Solo las incidencias criticas pueden ser EXPEDITE");
        }
        claseServicio = ClaseServicio.EXPEDITE;
    }

    public EstadoIncidencia getEstado() {
        return estado;
    }

    public void cambiarEstado(EstadoIncidencia nuevoEstado) {
        ValidadorTransicion.validar(estado, nuevoEstado, solucionAplicada);
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
