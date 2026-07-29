package cr.ac.utn.helpdeskflow.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.Incidencia;

/** Abstracción de persistencia para las incidencias. */
public interface IncidenciaRepository {

    /** Guarda una incidencia nueva o actualiza la existente. */
    void guardar(Incidencia incidencia);

    /** Devuelve todas las incidencias disponibles. */
    List<Incidencia> buscarTodas();

    /** Busca una incidencia por su identificador. */
    Optional<Incidencia> buscarPorId(UUID id);

    /** Ejecuta una operación con exclusión mutua en la implementación. */
    default void ejecutarAtomico(Runnable operacion) {
        synchronized (this) {
            operacion.run();
        }
    }
}
