package cr.ac.utn.helpdeskflow.application;

import cr.ac.utn.helpdeskflow.domain.ClaseServicio;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class ExpeditePolicy {

    private final IncidenciaRepository repository;

    public ExpeditePolicy(IncidenciaRepository repository) {
        this.repository = repository;
    }

    public void validarTransicion(Incidencia incidencia, EstadoIncidencia nuevoEstado) {
        if (!esExpedite(incidencia) || !esEstadoActivo(nuevoEstado)) {
            return;
        }

        boolean existeOtraExpediteActiva = repository.buscarTodas().stream()
                .filter(otra -> !otra.getId().equals(incidencia.getId()))
                .anyMatch(otra -> esExpedite(otra) && esEstadoActivo(otra.getEstado()));
        if (existeOtraExpediteActiva) {
            throw new ReglaNegocioException("Ya existe otra incidencia EXPEDITE en el estado indicado");
        }
    }

    private static boolean esExpedite(Incidencia incidencia) {
        return incidencia.getClaseServicio() == ClaseServicio.EXPEDITE;
    }

    private static boolean esEstadoActivo(EstadoIncidencia estado) {
        return estado == EstadoIncidencia.EN_DESARROLLO || estado == EstadoIncidencia.EN_VALIDACION;
    }
}
