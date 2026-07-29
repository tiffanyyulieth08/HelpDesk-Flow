package cr.ac.utn.helpdeskflow.application;

import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.ClaseServicio;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class IncidenciaWorkflowService {

    private final IncidenciaRepository repository;

    public IncidenciaWorkflowService(IncidenciaRepository repository) {
        this.repository = repository;
    }

    public void cambiarEstado(UUID id, EstadoIncidencia nuevoEstado) {
        Incidencia incidencia = repository.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe una incidencia con el identificador indicado"));

        validarLimiteExpedite(incidencia, nuevoEstado);
        incidencia.cambiarEstado(nuevoEstado);
        repository.guardar(incidencia);
    }

    private void validarLimiteExpedite(Incidencia incidencia, EstadoIncidencia nuevoEstado) {
        if (incidencia.getClaseServicio() != ClaseServicio.EXPEDITE
                || !esEstadoActivoExpedite(nuevoEstado)) {
            return;
        }

        boolean existeOtraExpediteActiva = repository.buscarTodas().stream()
                .filter(otra -> !otra.getId().equals(incidencia.getId()))
                .anyMatch(otra -> otra.getClaseServicio() == ClaseServicio.EXPEDITE
                        && esEstadoActivoExpedite(otra.getEstado()));
        if (existeOtraExpediteActiva) {
            throw new ReglaNegocioException("Ya existe otra incidencia EXPEDITE en el estado indicado");
        }
    }

    private static boolean esEstadoActivoExpedite(EstadoIncidencia estado) {
        return estado == EstadoIncidencia.EN_DESARROLLO || estado == EstadoIncidencia.EN_VALIDACION;
    }
}
