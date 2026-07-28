package cr.ac.utn.helpdeskflow.domain;

public final class CalculadoraPrioridad {

    private CalculadoraPrioridad() {
    }

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
