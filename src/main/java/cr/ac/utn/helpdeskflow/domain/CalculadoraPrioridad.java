package cr.ac.utn.helpdeskflow.domain;

public final class CalculadoraPrioridad {

    private CalculadoraPrioridad() {
    }

    public static Prioridad calcular(Impacto impacto, Urgencia urgencia) {
        if (impacto == Impacto.ALTO && urgencia == Urgencia.ALTA) {
            return Prioridad.CRITICA;
        }

        if (impacto == Impacto.ALTO || urgencia == Urgencia.ALTA) {
            return Prioridad.ALTA;
        }

        return Prioridad.NORMAL;
    }
}
