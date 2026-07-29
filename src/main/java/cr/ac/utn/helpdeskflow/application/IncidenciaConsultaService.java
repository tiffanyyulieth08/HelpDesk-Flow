package cr.ac.utn.helpdeskflow.application;

import java.util.List;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.CalculadoraPrioridad;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class IncidenciaConsultaService {

    private final IncidenciaRepository repository;

    public IncidenciaConsultaService(IncidenciaRepository repository) {
        this.repository = repository;
    }

    public List<Incidencia> listarTodas() {
        return repository.buscarTodas();
    }

    public Incidencia buscarPorId(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe una incidencia con el identificador indicado"));
    }

    public List<Incidencia> filtrarPorEstado(EstadoIncidencia estado) {
        return repository.buscarTodas().stream()
                .filter(incidencia -> incidencia.getEstado() == estado)
                .toList();
    }

    public List<Incidencia> filtrarPorPrioridad(Prioridad prioridad) {
        return repository.buscarTodas().stream()
                .filter(incidencia -> CalculadoraPrioridad.calcular(
                        incidencia.getImpacto(),
                        incidencia.getUrgencia()) == prioridad)
                .toList();
    }

    public List<Incidencia> listarAbiertas() {
        return repository.buscarTodas().stream()
                .filter(incidencia -> incidencia.getEstado() != EstadoIncidencia.FINALIZADA)
                .toList();
    }

    public List<Incidencia> listarFinalizadas() {
        return filtrarPorEstado(EstadoIncidencia.FINALIZADA);
    }
}
