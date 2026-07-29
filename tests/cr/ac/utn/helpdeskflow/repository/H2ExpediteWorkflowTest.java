package cr.ac.utn.helpdeskflow.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import cr.ac.utn.helpdeskflow.application.IncidenciaWorkflowService;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.infrastructure.H2IncidenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class H2ExpediteWorkflowTest {

    @TempDir
    Path carpetaTemporal;

    @Test
    void rechazaIntentosConsecutivosDeActivarDosExpeditePersistidas() {
        String url = "jdbc:h2:file:"
                + carpetaTemporal.resolve("flujo-expedite").toAbsolutePath().toString().replace('\\', '/');
        IncidenciaRepository repository = new H2IncidenciaRepository(url, "sa", "");
        IncidenciaWorkflowService workflow = new IncidenciaWorkflowService(repository);
        Incidencia primera = crearExpedite("Primera incidencia critica");
        Incidencia segunda = crearExpedite("Segunda incidencia critica");
        repository.guardar(primera);
        repository.guardar(segunda);
        workflow.cambiarEstado(primera.getId(), EstadoIncidencia.LISTA);
        workflow.cambiarEstado(segunda.getId(), EstadoIncidencia.LISTA);

        workflow.cambiarEstado(primera.getId(), EstadoIncidencia.EN_DESARROLLO);

        assertThrows(ReglaNegocioException.class,
                () -> workflow.cambiarEstado(segunda.getId(), EstadoIncidencia.EN_DESARROLLO));
        assertEquals(EstadoIncidencia.EN_DESARROLLO,
                repository.buscarPorId(primera.getId()).orElseThrow().getEstado());
        assertEquals(EstadoIncidencia.LISTA,
                repository.buscarPorId(segunda.getId()).orElseThrow().getEstado());
    }

    private static Incidencia crearExpedite(String titulo) {
        Incidencia incidencia = Incidencia.crear(
                titulo,
                "Descripcion suficientemente extensa para probar el flujo",
                "Infraestructura", Impacto.ALTO, Urgencia.ALTA);
        incidencia.marcarComoExpedite();
        return incidencia;
    }
}
