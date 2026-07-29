package cr.ac.utn.helpdeskflow.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.ClaseServicio;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.infrastructure.InMemoryIncidenciaRepository;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpediteWorkflowTest {

    private IncidenciaRepository repository;
    private IncidenciaWorkflowService workflow;

    @BeforeEach
    void configurar() {
        repository = new InMemoryIncidenciaRepository();
        workflow = new IncidenciaWorkflowService(repository);
    }

    @Test
    void permiteVariasExpediteRegistradasOListas() {
        Incidencia primera = registrarExpedite("Primer servicio critico");
        Incidencia segunda = registrarExpedite("Segundo servicio critico");

        workflow.cambiarEstado(primera.getId(), EstadoIncidencia.LISTA);
        workflow.cambiarEstado(segunda.getId(), EstadoIncidencia.LISTA);

        assertEquals(EstadoIncidencia.LISTA, primera.getEstado());
        assertEquals(EstadoIncidencia.LISTA, segunda.getEstado());
    }

    @Test
    void permitePrimeraExpediteEnDesarrollo() {
        Incidencia expedite = registrarExpedite("Servicio principal caido");

        avanzarHasta(expedite, EstadoIncidencia.EN_DESARROLLO);

        assertEquals(EstadoIncidencia.EN_DESARROLLO, expedite.getEstado());
    }

    @Test
    void rechazaSegundaExpediteEnDesarrollo() {
        Incidencia primera = registrarExpedite("Primera falla critica");
        Incidencia segunda = registrarExpedite("Segunda falla critica");
        avanzarHasta(primera, EstadoIncidencia.EN_DESARROLLO);

        assertThrows(ReglaNegocioException.class,
                () -> avanzarHasta(segunda, EstadoIncidencia.EN_DESARROLLO));
        assertEquals(EstadoIncidencia.LISTA, segunda.getEstado());
    }

    @Test
    void rechazaSegundaExpediteCuandoOtraEstaEnValidacion() {
        Incidencia primera = registrarExpedite("Primera validacion critica");
        Incidencia segunda = registrarExpedite("Segunda validacion critica");
        avanzarHasta(primera, EstadoIncidencia.EN_VALIDACION);

        assertThrows(ReglaNegocioException.class,
                () -> avanzarHasta(segunda, EstadoIncidencia.EN_DESARROLLO));
        assertEquals(EstadoIncidencia.LISTA, segunda.getEstado());
    }

    @Test
    void permiteIncidenciaNormalAunqueExistaExpediteActiva() {
        Incidencia expedite = registrarExpedite("Expedite activa");
        Incidencia normal = registrarNormal("Incidencia normal pendiente");
        avanzarHasta(expedite, EstadoIncidencia.EN_DESARROLLO);

        avanzarHasta(normal, EstadoIncidencia.EN_DESARROLLO);

        assertEquals(EstadoIncidencia.EN_DESARROLLO, normal.getEstado());
        assertEquals(ClaseServicio.NORMAL, normal.getClaseServicio());
    }

    @Test
    void permiteOtraExpediteCuandoLaAnteriorFinaliza() {
        Incidencia primera = registrarExpedite("Expedite resuelta");
        Incidencia segunda = registrarExpedite("Expedite siguiente");
        avanzarHasta(primera, EstadoIncidencia.EN_VALIDACION);
        primera.registrarSolucion("Se aplico la solucion definitiva");
        workflow.cambiarEstado(primera.getId(), EstadoIncidencia.FINALIZADA);

        avanzarHasta(segunda, EstadoIncidencia.EN_DESARROLLO);

        assertEquals(EstadoIncidencia.EN_DESARROLLO, segunda.getEstado());
    }

    private Incidencia registrarExpedite(String titulo) {
        Incidencia incidencia = Incidencia.crear(titulo,
                "La descripcion de la incidencia tiene suficiente detalle",
                "Infraestructura", Impacto.ALTO, Urgencia.ALTA);
        incidencia.marcarComoExpedite();
        repository.guardar(incidencia);
        return incidencia;
    }

    private Incidencia registrarNormal(String titulo) {
        Incidencia incidencia = Incidencia.crear(titulo,
                "La descripcion de la incidencia tiene suficiente detalle",
                "Soporte", Impacto.MEDIO, Urgencia.MEDIA);
        repository.guardar(incidencia);
        return incidencia;
    }

    private void avanzarHasta(Incidencia incidencia, EstadoIncidencia destino) {
        while (incidencia.getEstado() != destino) {
            EstadoIncidencia siguiente = EstadoIncidencia.values()[incidencia.getEstado().ordinal() + 1];
            workflow.cambiarEstado(incidencia.getId(), siguiente);
        }
    }
}
