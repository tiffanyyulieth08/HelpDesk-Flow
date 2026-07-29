package cr.ac.utn.helpdeskflow.application;

import java.time.Duration;
import java.util.Map;

import cr.ac.utn.helpdeskflow.domain.Prioridad;

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

    public int getTotal() {
        return total;
    }

    public int getAbiertas() {
        return abiertas;
    }

    public int getFinalizadas() {
        return finalizadas;
    }

    public int getThroughput() {
        return throughput;
    }

    public Duration getLeadTimePromedio() {
        return leadTimePromedio;
    }

    public Map<Prioridad, Integer> getCantidadPorPrioridad() {
        return cantidadPorPrioridad;
    }
}
