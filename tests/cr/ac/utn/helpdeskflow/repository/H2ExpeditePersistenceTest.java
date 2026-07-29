package cr.ac.utn.helpdeskflow.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import cr.ac.utn.helpdeskflow.domain.ClaseServicio;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.infrastructure.H2IncidenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class H2ExpeditePersistenceTest {

    @TempDir
    Path carpetaTemporal;

    @Test
    void conservaClaseServicioExpediteEnH2() {
        String jdbcUrl = "jdbc:h2:file:"
                + carpetaTemporal.resolve("expedite").toAbsolutePath().toString().replace('\\', '/');
        IncidenciaRepository repository = new H2IncidenciaRepository(jdbcUrl, "sa", "");
        Incidencia incidencia = Incidencia.crear(
                "Servidor sin respuesta",
                "La descripcion de la incidencia tiene suficiente detalle",
                "Infraestructura", Impacto.ALTO, Urgencia.ALTA);
        incidencia.marcarComoExpedite();

        repository.guardar(incidencia);

        Incidencia recuperada = repository.buscarPorId(incidencia.getId()).orElseThrow();
        assertEquals(ClaseServicio.EXPEDITE, recuperada.getClaseServicio());
    }
}
