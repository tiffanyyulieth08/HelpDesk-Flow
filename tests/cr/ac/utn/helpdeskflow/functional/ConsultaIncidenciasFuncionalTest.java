package cr.ac.utn.helpdeskflow.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import cr.ac.utn.helpdeskflow.application.IncidenciaConsultaService;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.infrastructure.InMemoryIncidenciaRepository;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;
import org.junit.jupiter.api.Test;

class ConsultaIncidenciasFuncionalTest {

    @Test
    void permiteRegistrarConsultarYFiltrarIncidencias() {
        IncidenciaRepository repository = new InMemoryIncidenciaRepository();
        IncidenciaConsultaService service = new IncidenciaConsultaService(repository);
        Incidencia critica = crearIncidencia("Servicio principal caido", Impacto.ALTO, Urgencia.ALTA);
        Incidencia alta = crearIncidencia("Correo institucional lento", Impacto.MEDIO, Urgencia.ALTA);
        Incidencia normal = crearIncidencia("Mouse por reemplazar", Impacto.BAJO, Urgencia.BAJA);
        repository.guardar(critica);
        repository.guardar(alta);
        repository.guardar(normal);

        List<Incidencia> todas = service.listarTodas();
        Incidencia encontrada = service.buscarPorId(alta.getId());
        List<Incidencia> criticas = service.filtrarPorPrioridad(Prioridad.CRITICA);

        assertEquals(Set.of(critica, alta, normal), Set.copyOf(todas));
        assertSame(alta, encontrada);
        assertEquals(List.of(critica), criticas);
    }

    @Test
    void separaIncidenciasAbiertasYFinalizadas() {
        IncidenciaRepository repository = new InMemoryIncidenciaRepository();
        IncidenciaConsultaService service = new IncidenciaConsultaService(repository);
        Incidencia abierta = crearIncidencia("Problema de acceso remoto", Impacto.MEDIO, Urgencia.MEDIA);
        Incidencia finalizada = crearIncidencia("Configuracion de impresora", Impacto.BAJO, Urgencia.BAJA);
        finalizar(finalizada);
        repository.guardar(abierta);
        repository.guardar(finalizada);

        List<Incidencia> abiertas = service.listarAbiertas();
        List<Incidencia> finalizadas = service.listarFinalizadas();

        assertEquals(List.of(abierta), abiertas);
        assertEquals(List.of(finalizada), finalizadas);
        assertTrue(abiertas.stream()
                .allMatch(incidencia -> incidencia.getEstado() != EstadoIncidencia.FINALIZADA));
        assertTrue(finalizadas.stream()
                .allMatch(incidencia -> incidencia.getEstado() == EstadoIncidencia.FINALIZADA));
    }

    private static Incidencia crearIncidencia(String titulo, Impacto impacto, Urgencia urgencia) {
        return Incidencia.crear(
                titulo,
                "Descripcion valida para la incidencia funcional",
                "Soporte",
                impacto,
                urgencia);
    }

    private static void finalizar(Incidencia incidencia) {
        incidencia.cambiarEstado(EstadoIncidencia.LISTA);
        incidencia.cambiarEstado(EstadoIncidencia.EN_DESARROLLO);
        incidencia.cambiarEstado(EstadoIncidencia.EN_VALIDACION);
        incidencia.registrarSolucion("Se valido y aplico la solucion definitiva");
        incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA);
    }
}
