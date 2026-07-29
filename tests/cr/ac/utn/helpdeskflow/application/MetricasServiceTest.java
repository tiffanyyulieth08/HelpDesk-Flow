package cr.ac.utn.helpdeskflow.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.ClaseServicio;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.infrastructure.InMemoryIncidenciaRepository;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricasServiceTest {

    private IncidenciaRepository repository;
    private MetricasService service;

    @BeforeEach
    void configurar() {
        repository = new InMemoryIncidenciaRepository();
        service = new MetricasService(repository);
    }

    @Test
    void metricasDeRepositorioVacioSonCero() {
        MetricasResumen resumen = service.calcular();

        assertEquals(0, resumen.getTotal());
        assertEquals(0, resumen.getAbiertas());
        assertEquals(0, resumen.getFinalizadas());
        assertEquals(0, resumen.getThroughput());
        assertEquals(Duration.ZERO, resumen.getLeadTimePromedio());
        assertEquals(Map.of(
                Prioridad.NORMAL, 0,
                Prioridad.ALTA, 0,
                Prioridad.CRITICA, 0), resumen.getCantidadPorPrioridad());
    }

    @Test
    void calculaCantidadTotalDeIncidencias() {
        guardar("Registrada", Impacto.BAJO, Urgencia.BAJA);
        guardar("Otra registrada", Impacto.MEDIO, Urgencia.MEDIA);
        guardar("Incidencia critica", Impacto.ALTO, Urgencia.ALTA);

        assertEquals(3, service.calcular().getTotal());
    }

    @Test
    void calculaCantidadDeIncidenciasAbiertas() {
        for (EstadoIncidencia estado : List.of(
                EstadoIncidencia.REGISTRADA,
                EstadoIncidencia.LISTA,
                EstadoIncidencia.EN_DESARROLLO,
                EstadoIncidencia.EN_VALIDACION)) {
            Incidencia incidencia = guardar("Abierta " + estado, Impacto.MEDIO, Urgencia.MEDIA);
            avanzarHasta(incidencia, estado);
        }
        Incidencia finalizada = guardar("Finalizada", Impacto.MEDIO, Urgencia.MEDIA);
        finalizar(finalizada);

        assertEquals(4, service.calcular().getAbiertas());
    }

    @Test
    void calculaCantidadDeIncidenciasFinalizadas() {
        Incidencia finalizada = guardar("Finalizada", Impacto.BAJO, Urgencia.BAJA);
        finalizar(finalizada);
        guardar("Abierta", Impacto.MEDIO, Urgencia.MEDIA);

        assertEquals(1, service.calcular().getFinalizadas());
    }

    @Test
    void calculaThroughputComoCantidadFinalizada() {
        Incidencia primera = guardar("Primera finalizada", Impacto.BAJO, Urgencia.BAJA);
        Incidencia segunda = guardar("Segunda finalizada", Impacto.MEDIO, Urgencia.MEDIA);
        finalizar(primera);
        finalizar(segunda);
        guardar("Pendiente", Impacto.ALTO, Urgencia.MEDIA);

        assertEquals(2, service.calcular().getThroughput());
    }

    @Test
    void calculaLeadTimePromedioDeFinalizadas() {
        LocalDateTime inicio = LocalDateTime.of(2026, 7, 1, 8, 0);
        Incidencia primera = rehidratarConFechas(
                "Primera", inicio, inicio.plusHours(2), EstadoIncidencia.FINALIZADA,
                Impacto.BAJO, Urgencia.BAJA);
        Incidencia segunda = rehidratarConFechas(
                "Segunda", inicio, inicio.plusHours(4), EstadoIncidencia.FINALIZADA,
                Impacto.MEDIO, Urgencia.MEDIA);
        repository.guardar(primera);
        repository.guardar(segunda);

        assertEquals(Duration.ofHours(3), service.calcular().getLeadTimePromedio());
    }

    @Test
    void ignoraIncidenciasAbiertasEnLeadTimePromedio() {
        LocalDateTime inicio = LocalDateTime.of(2026, 7, 2, 8, 0);
        Incidencia finalizada = rehidratarConFechas(
                "Finalizada", inicio, inicio.plusHours(2), EstadoIncidencia.FINALIZADA,
                Impacto.BAJO, Urgencia.BAJA);
        repository.guardar(finalizada);
        repository.guardar(rehidratarConFechas(
                "Abierta", inicio, null, EstadoIncidencia.EN_DESARROLLO,
                Impacto.MEDIO, Urgencia.MEDIA));

        assertEquals(Duration.ofHours(2), service.calcular().getLeadTimePromedio());
    }

    @Test
    void agrupaIncidenciasPorPrioridad() {
        guardar("Normal", Impacto.BAJO, Urgencia.BAJA);
        guardar("Alta por impacto", Impacto.ALTO, Urgencia.MEDIA);
        guardar("Critica", Impacto.ALTO, Urgencia.ALTA);
        guardar("Otra alta", Impacto.MEDIO, Urgencia.ALTA);

        assertEquals(Map.of(
                Prioridad.NORMAL, 1,
                Prioridad.ALTA, 2,
                Prioridad.CRITICA, 1), service.calcular().getCantidadPorPrioridad());
    }

    @Test
    void calcularMetricasNoModificaElRepositorio() {
        Incidencia abierta = guardar("Abierta", Impacto.BAJO, Urgencia.BAJA);
        Incidencia finalizada = guardar("Finalizada", Impacto.ALTO, Urgencia.ALTA);
        finalizar(finalizada);
        List<Incidencia> originales = repository.buscarTodas();
        List<UUID> identificadores = originales.stream().map(Incidencia::getId).toList();
        List<EstadoIncidencia> estados = originales.stream().map(Incidencia::getEstado).toList();

        service.calcular();

        List<Incidencia> actuales = repository.buscarTodas();
        assertEquals(originales.size(), actuales.size());
        assertEquals(identificadores, actuales.stream().map(Incidencia::getId).toList());
        assertEquals(estados, actuales.stream().map(Incidencia::getEstado).toList());
        assertEquals(abierta.getId(), actuales.get(0).getId());
    }

    private Incidencia guardar(String titulo, Impacto impacto, Urgencia urgencia) {
        Incidencia incidencia = crearIncidencia(titulo, impacto, urgencia);
        repository.guardar(incidencia);
        return incidencia;
    }

    private static Incidencia crearIncidencia(String titulo, Impacto impacto, Urgencia urgencia) {
        return Incidencia.crear(
                titulo,
                "Descripcion valida para la incidencia de prueba",
                "Soporte",
                impacto,
                urgencia);
    }

    private static Incidencia rehidratarConFechas(
            String titulo,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaCierre,
            EstadoIncidencia estado,
            Impacto impacto,
            Urgencia urgencia) {
        return Incidencia.rehidratar(
                UUID.randomUUID(),
                titulo,
                "Descripcion valida para la incidencia de prueba",
                "Soporte",
                impacto,
                urgencia,
                estado,
                "Se aplico una solucion verificada",
                fechaCreacion,
                fechaCierre,
                ClaseServicio.NORMAL);
    }

    private static void avanzarHasta(Incidencia incidencia, EstadoIncidencia estadoObjetivo) {
        while (incidencia.getEstado() != estadoObjetivo) {
            EstadoIncidencia siguiente = EstadoIncidencia.values()[incidencia.getEstado().ordinal() + 1];
            incidencia.cambiarEstado(siguiente);
        }
    }

    private static void finalizar(Incidencia incidencia) {
        avanzarHasta(incidencia, EstadoIncidencia.EN_VALIDACION);
        incidencia.registrarSolucion("Se aplico una solucion verificada");
        incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA);
    }
}
