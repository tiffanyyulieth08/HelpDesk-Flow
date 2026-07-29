package cr.ac.utn.helpdeskflow.application;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

/** Provee consultas de solo lectura sobre las incidencias registradas. */
public class IncidenciaConsultaService {

    private final IncidenciaRepository repository;

    public IncidenciaConsultaService(IncidenciaRepository repository) {
        this.repository = repository;
    }

    /** Devuelve todas las incidencias en el orden del repositorio. */
    public List<Incidencia> listarTodas() {
        return repository.buscarTodas();
    }

    /** Busca una incidencia o informa una regla de negocio si no existe. */
    public Incidencia buscarPorId(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe una incidencia con el identificador indicado"));
    }

    /** Devuelve las incidencias que se encuentran en el estado indicado. */
    public List<Incidencia> filtrarPorEstado(EstadoIncidencia estado) {
        return filtrar(incidencia -> incidencia.getEstado() == estado);
    }

    /** Devuelve las incidencias cuya prioridad calculada coincide con la indicada. */
    public List<Incidencia> filtrarPorPrioridad(Prioridad prioridad) {
        return filtrar(incidencia -> incidencia.getPrioridad() == prioridad);
    }

    /** Devuelve las incidencias que todavía no han sido finalizadas. */
    public List<Incidencia> listarAbiertas() {
        return filtrar(IncidenciaConsultaService::esAbierta);
    }

    /** Devuelve las incidencias en estado FINALIZADA. */
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
