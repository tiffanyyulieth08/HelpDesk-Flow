package cr.ac.utn.helpdeskflow.application;

import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

/** Coordina las operaciones que modifican el estado de una incidencia. */
public class IncidenciaWorkflowService {

    private final IncidenciaRepository repository;
    private final ExpeditePolicy expeditePolicy;

    public IncidenciaWorkflowService(IncidenciaRepository repository) {
        this.repository = repository;
        this.expeditePolicy = new ExpeditePolicy(repository);
    }

    /** Cambia el estado aplicando las reglas de transición y de exclusividad EXPEDITE. */
    public void cambiarEstado(UUID id, EstadoIncidencia nuevoEstado) {
        repository.ejecutarAtomico(() -> {
            Incidencia incidencia = repository.buscarPorId(id)
                    .orElseThrow(() -> new ReglaNegocioException(
                            "No existe una incidencia con el identificador indicado"));

            expeditePolicy.validarTransicion(incidencia, nuevoEstado);
            incidencia.cambiarEstado(nuevoEstado);
            repository.guardar(incidencia);
        });
    }

    /** Registra o reemplaza la solución asociada a una incidencia. */
    public void registrarSolucion(UUID id, String solucion) {
        repository.ejecutarAtomico(() -> {
            Incidencia incidencia = obtenerIncidencia(id);
            incidencia.registrarSolucion(solucion);
            repository.guardar(incidencia);
        });
    }

    /** Marca como EXPEDITE una incidencia con prioridad CRITICA. */
    public void marcarComoExpedite(UUID id) {
        repository.ejecutarAtomico(() -> {
            Incidencia incidencia = obtenerIncidencia(id);
            incidencia.marcarComoExpedite();
            repository.guardar(incidencia);
        });
    }

    private Incidencia obtenerIncidencia(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No existe una incidencia con el identificador indicado"));
    }
}
