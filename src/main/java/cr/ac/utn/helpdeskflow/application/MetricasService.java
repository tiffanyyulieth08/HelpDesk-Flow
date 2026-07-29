package cr.ac.utn.helpdeskflow.application;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

/** Calcula indicadores operativos a partir de las incidencias persistidas. */
public class MetricasService {

    private final IncidenciaRepository repository;

    public MetricasService(IncidenciaRepository repository) {
        this.repository = repository;
    }

    /** Calcula el resumen actual de volumen, estados, throughput y lead time. */
    public MetricasResumen calcular() {
        List<Incidencia> incidencias = repository.buscarTodas();
        EnumMap<Prioridad, Integer> cantidadPorPrioridad = crearConteosPorPrioridad();

        int abiertas = 0;
        int finalizadas = 0;
        Duration leadTimeTotal = Duration.ZERO;

        for (Incidencia incidencia : incidencias) {
            contarPorPrioridad(cantidadPorPrioridad, incidencia);
            if (esFinalizada(incidencia)) {
                finalizadas++;
                leadTimeTotal = leadTimeTotal.plus(
                        Duration.between(incidencia.getFechaCreacion(), incidencia.getFechaCierre()));
            } else {
                abiertas++;
            }
        }

        return crearResumen(incidencias.size(), abiertas, finalizadas, leadTimeTotal,
                cantidadPorPrioridad);
    }

    private EnumMap<Prioridad, Integer> crearConteosPorPrioridad() {
        EnumMap<Prioridad, Integer> conteos = new EnumMap<>(Prioridad.class);
        for (Prioridad prioridad : Prioridad.values()) {
            conteos.put(prioridad, 0);
        }
        return conteos;
    }

    private void contarPorPrioridad(Map<Prioridad, Integer> conteos, Incidencia incidencia) {
        conteos.compute(incidencia.getPrioridad(), (prioridad, cantidad) -> cantidad + 1);
    }

    private boolean esFinalizada(Incidencia incidencia) {
        return incidencia.getEstado() == EstadoIncidencia.FINALIZADA;
    }

    private MetricasResumen crearResumen(int total, int abiertas, int finalizadas,
                                         Duration leadTimeTotal,
                                         Map<Prioridad, Integer> cantidadPorPrioridad) {
        Duration leadTimePromedio = calcularLeadTimePromedio(leadTimeTotal, finalizadas);
        return new MetricasResumen(total, abiertas, finalizadas, finalizadas,
                leadTimePromedio, cantidadPorPrioridad);
    }

    private Duration calcularLeadTimePromedio(Duration leadTimeTotal, int finalizadas) {
        return finalizadas == 0 ? Duration.ZERO : leadTimeTotal.dividedBy(finalizadas);
    }
}
