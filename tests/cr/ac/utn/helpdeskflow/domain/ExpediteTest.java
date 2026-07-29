package cr.ac.utn.helpdeskflow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class ExpediteTest {

    @Test
    void incidenciaNuevaIniciaComoNormal() {
        Incidencia incidencia = crearIncidencia(Impacto.MEDIO, Urgencia.MEDIA);

        assertEquals(ClaseServicio.NORMAL, incidencia.getClaseServicio());
    }

    @Test
    void permiteMarcarIncidenciaCriticaComoExpedite() {
        Incidencia incidencia = crearIncidencia(Impacto.ALTO, Urgencia.ALTA);
        Prioridad prioridadAntes = incidencia.getPrioridad();

        incidencia.marcarComoExpedite();

        assertEquals(ClaseServicio.EXPEDITE, incidencia.getClaseServicio());
        assertEquals(Prioridad.CRITICA, prioridadAntes);
        assertEquals(prioridadAntes, incidencia.getPrioridad());
    }

    @Test
    void rechazaMarcarIncidenciaAltaComoExpedite() {
        Incidencia incidencia = crearIncidencia(Impacto.ALTO, Urgencia.MEDIA);

        assertThrows(ReglaNegocioException.class, incidencia::marcarComoExpedite);
        assertEquals(ClaseServicio.NORMAL, incidencia.getClaseServicio());
    }

    @Test
    void rechazaMarcarIncidenciaNormalComoExpedite() {
        Incidencia incidencia = crearIncidencia(Impacto.MEDIO, Urgencia.MEDIA);

        assertThrows(ReglaNegocioException.class, incidencia::marcarComoExpedite);
        assertEquals(ClaseServicio.NORMAL, incidencia.getClaseServicio());
    }

    private static Incidencia crearIncidencia(Impacto impacto, Urgencia urgencia) {
        return Incidencia.crear(
                "Servidor sin respuesta",
                "La descripcion de la incidencia tiene suficiente detalle",
                "Infraestructura",
                impacto,
                urgencia);
    }
}
