package cr.ac.utn.helpdeskflow.application;

import java.time.Duration;
import java.util.Map;

import cr.ac.utn.helpdeskflow.domain.Prioridad;

/** Resultado inmutable de las métricas calculadas para las incidencias. */
public final class MetricasResumen {

    private final int total;
    private final int abiertas;
    private final int finalizadas;
    private final int throughput;
    private final Duration leadTimePromedio;
    private final Map<Prioridad, Integer> cantidadPorPrioridad;

    public MetricasResumen(int total, int abiertas, int finalizadas, int throughput,
                           Duration leadTimePromedio, Map<Prioridad, Integer> cantidadPorPrioridad) {
        this.total = total;
        this.abiertas = abiertas;
        this.finalizadas = finalizadas;
        this.throughput = throughput;
        this.leadTimePromedio = leadTimePromedio;
        this.cantidadPorPrioridad = Map.copyOf(cantidadPorPrioridad);
    }

    /** Devuelve el total de incidencias. */
    public int getTotal() {
        return total;
    }

    /** Devuelve la cantidad de incidencias no finalizadas. */
    public int getAbiertas() {
        return abiertas;
    }

    /** Devuelve la cantidad de incidencias finalizadas. */
    public int getFinalizadas() {
        return finalizadas;
    }

    /** Devuelve la cantidad de incidencias finalizadas en el período consultado. */
    public int getThroughput() {
        return throughput;
    }

    /** Devuelve el tiempo promedio desde creación hasta cierre. */
    public Duration getLeadTimePromedio() {
        return leadTimePromedio;
    }

    /** Devuelve un mapa inmutable con el conteo por prioridad. */
    public Map<Prioridad, Integer> getCantidadPorPrioridad() {
        return cantidadPorPrioridad;
    }
}
