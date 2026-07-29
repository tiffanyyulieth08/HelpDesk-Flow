package cr.ac.utn.helpdeskflow.domain;

/** Calcula la prioridad combinando impacto y urgencia. */
public final class CalculadoraPrioridad {

    private CalculadoraPrioridad() {
    }

    /** Devuelve CRITICA, ALTA o NORMAL según la matriz de prioridad del sistema. */
    public static Prioridad calcular(Impacto impacto, Urgencia urgencia) {
        if (esCritica(impacto, urgencia)) {
            return Prioridad.CRITICA;
        }

        if (requierePrioridadAlta(impacto, urgencia)) {
            return Prioridad.ALTA;
        }

        return Prioridad.NORMAL;
    }

    private static boolean esCritica(Impacto impacto, Urgencia urgencia) {
        return impacto == Impacto.ALTO && urgencia == Urgencia.ALTA;
    }

    private static boolean requierePrioridadAlta(Impacto impacto, Urgencia urgencia) {
        return impacto == Impacto.ALTO || urgencia == Urgencia.ALTA;
    }
}
