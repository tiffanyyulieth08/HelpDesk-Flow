package cr.ac.utn.helpdeskflow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CalculadoraPrioridadTest {

    @Test
    void impactoAltoUrgenciaAltaProduceCritica() {
        assertEquals(Prioridad.CRITICA, CalculadoraPrioridad.calcular(Impacto.ALTO, Urgencia.ALTA));
    }

    @Test
    void impactoAltoUrgenciaMediaProduceAlta() {
        assertEquals(Prioridad.ALTA, CalculadoraPrioridad.calcular(Impacto.ALTO, Urgencia.MEDIA));
    }

    @Test
    void impactoAltoUrgenciaBajaProduceAlta() {
        assertEquals(Prioridad.ALTA, CalculadoraPrioridad.calcular(Impacto.ALTO, Urgencia.BAJA));
    }

    @Test
    void impactoMedioUrgenciaAltaProduceAlta() {
        assertEquals(Prioridad.ALTA, CalculadoraPrioridad.calcular(Impacto.MEDIO, Urgencia.ALTA));
    }

    @Test
    void impactoBajoUrgenciaAltaProduceAlta() {
        assertEquals(Prioridad.ALTA, CalculadoraPrioridad.calcular(Impacto.BAJO, Urgencia.ALTA));
    }

    @Test
    void impactoMedioUrgenciaMediaProduceNormal() {
        assertEquals(Prioridad.NORMAL, CalculadoraPrioridad.calcular(Impacto.MEDIO, Urgencia.MEDIA));
    }

    @Test
    void impactoMedioUrgenciaBajaProduceNormal() {
        assertEquals(Prioridad.NORMAL, CalculadoraPrioridad.calcular(Impacto.MEDIO, Urgencia.BAJA));
    }

    @Test
    void impactoBajoUrgenciaMediaProduceNormal() {
        assertEquals(Prioridad.NORMAL, CalculadoraPrioridad.calcular(Impacto.BAJO, Urgencia.MEDIA));
    }

    @Test
    void impactoBajoUrgenciaBajaProduceNormal() {
        assertEquals(Prioridad.NORMAL, CalculadoraPrioridad.calcular(Impacto.BAJO, Urgencia.BAJA));
    }
}
