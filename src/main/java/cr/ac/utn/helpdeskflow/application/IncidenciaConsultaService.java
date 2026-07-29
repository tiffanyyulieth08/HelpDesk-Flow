package cr.ac.utn.helpdeskflow.application;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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
        return filtrar(incidencia -> incidencia.getEstado() == estado);
    }

    public List<Incidencia> filtrarPorPrioridad(Prioridad prioridad) {
        return filtrar(incidencia -> CalculadoraPrioridad.calcular(
                incidencia.getImpacto(),
                incidencia.getUrgencia()) == prioridad);
    }

    public List<Incidencia> listarAbiertas() {
        return filtrar(IncidenciaConsultaService::esAbierta);
    }

    public List<Incidencia> listarFinalizadas() {
        return filtrarPorEstado(EstadoIncidencia.FINALIZADA);
    }

    private List<Incidencia> filtrar(Predicate<Incidencia> criterio) {
        return repository.buscarTodas().stream()
                .filter(criterio)
                .toList();
    }

    private static boolean esAbierta(Incidencia incidencia) {
        return incidencia.getEstado() != EstadoIncidencia.FINALIZADA;
    }
}
