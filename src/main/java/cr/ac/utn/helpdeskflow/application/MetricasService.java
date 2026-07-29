package cr.ac.utn.helpdeskflow.application;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class MetricasService {

    private final IncidenciaRepository repository;

    public MetricasService(IncidenciaRepository repository) {
        this.repository = repository;
    }

    public MetricasResumen calcular() {
        List<Incidencia> incidencias = repository.buscarTodas();
        EnumMap<Prioridad, Integer> cantidadPorPrioridad = new EnumMap<>(Prioridad.class);
        for (Prioridad prioridad : Prioridad.values()) {
            cantidadPorPrioridad.put(prioridad, 0);
        }

        int abiertas = 0;
        int finalizadas = 0;
        Duration leadTimeTotal = Duration.ZERO;

        for (Incidencia incidencia : incidencias) {
            cantidadPorPrioridad.compute(incidencia.getPrioridad(), (prioridad, cantidad) -> cantidad + 1);
            if (incidencia.getEstado() == EstadoIncidencia.FINALIZADA) {
                finalizadas++;
                leadTimeTotal = leadTimeTotal.plus(
                        Duration.between(incidencia.getFechaCreacion(), incidencia.getFechaCierre()));
            } else {
                abiertas++;
            }
        }

        Duration leadTimePromedio = finalizadas == 0
                ? Duration.ZERO
                : leadTimeTotal.dividedBy(finalizadas);
        Map<Prioridad, Integer> cantidades = Map.copyOf(cantidadPorPrioridad);
        return new MetricasResumen(incidencias.size(), abiertas, finalizadas, finalizadas,
                leadTimePromedio, cantidades);
    }
}
