package cr.ac.utn.helpdeskflow.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.Incidencia;

public interface IncidenciaRepository {

    void guardar(Incidencia incidencia);

    List<Incidencia> buscarTodas();

    Optional<Incidencia> buscarPorId(UUID id);

    default void ejecutarAtomico(Runnable operacion) {
        synchronized (this) {
            operacion.run();
        }
    }
}
