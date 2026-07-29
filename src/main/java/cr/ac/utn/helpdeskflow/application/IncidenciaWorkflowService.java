package cr.ac.utn.helpdeskflow.application;

import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class IncidenciaWorkflowService {

    private final IncidenciaRepository repository;
    private final ExpeditePolicy expeditePolicy;

    public IncidenciaWorkflowService(IncidenciaRepository repository) {
        this.repository = repository;
        this.expeditePolicy = new ExpeditePolicy(repository);
    }

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
}
