package cr.ac.utn.helpdeskflow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class IncidenciaTransicionTest {

    @Test
    void permiteCambiarDeRegistradaALista() {
        Incidencia incidencia = crearIncidenciaValida();

        incidencia.cambiarEstado(EstadoIncidencia.LISTA);

        assertEquals(EstadoIncidencia.LISTA, incidencia.getEstado());
    }

    @Test
    void permiteCambiarDeListaAEnDesarrollo() {
        Incidencia incidencia = crearIncidenciaValida();
        incidencia.cambiarEstado(EstadoIncidencia.LISTA);

        incidencia.cambiarEstado(EstadoIncidencia.EN_DESARROLLO);

        assertEquals(EstadoIncidencia.EN_DESARROLLO, incidencia.getEstado());
    }

    @Test
    void permiteCambiarDeEnDesarrolloAEnValidacion() {
        Incidencia incidencia = incidenciaEnDesarrollo();

        incidencia.cambiarEstado(EstadoIncidencia.EN_VALIDACION);

        assertEquals(EstadoIncidencia.EN_VALIDACION, incidencia.getEstado());
    }

    @Test
    void permiteFinalizarCuandoExisteSolucion() {
        Incidencia incidencia = incidenciaEnValidacion();
        String solucion = "Se reinicio el servicio de red afectado";
        incidencia.registrarSolucion(solucion);

        incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA);

        assertEquals(EstadoIncidencia.FINALIZADA, incidencia.getEstado());
        assertEquals(solucion, incidencia.getSolucionAplicada());
    }

    @Test
    void rechazaSaltoDeRegistradaAFinalizada() {
        Incidencia incidencia = crearIncidenciaValida();

        assertThrows(ReglaNegocioException.class,
                () -> incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA));
    }

    @Test
    void rechazaRetrocesoDeEnDesarrolloALista() {
        Incidencia incidencia = incidenciaEnDesarrollo();

        assertThrows(ReglaNegocioException.class,
                () -> incidencia.cambiarEstado(EstadoIncidencia.LISTA));
    }

    @Test
    void rechazaCambioDeFinalizadaAEnDesarrollo() {
        Incidencia incidencia = incidenciaFinalizada();

        assertThrows(ReglaNegocioException.class,
                () -> incidencia.cambiarEstado(EstadoIncidencia.EN_DESARROLLO));
    }

    @Test
    void rechazaFinalizarSinSolucion() {
        Incidencia incidencia = incidenciaEnValidacion();

        assertThrows(ReglaNegocioException.class,
                () -> incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA));
    }

    @Test
    void registraFechaDeCierreAlFinalizar() {
        Incidencia incidencia = incidenciaEnValidacion();
        incidencia.registrarSolucion("Se aplico la actualizacion correctiva");

        incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA);

        assertNotNull(incidencia.getFechaCierre());
    }

    private static Incidencia crearIncidenciaValida() {
        return Incidencia.crear(
                "Conexion intermitente",
                "La conexion de red falla durante la jornada laboral",
                "Redes",
                Impacto.MEDIO,
                Urgencia.MEDIA);
    }

    private static Incidencia incidenciaEnDesarrollo() {
        Incidencia incidencia = crearIncidenciaValida();
        incidencia.cambiarEstado(EstadoIncidencia.LISTA);
        incidencia.cambiarEstado(EstadoIncidencia.EN_DESARROLLO);
        return incidencia;
    }

    private static Incidencia incidenciaEnValidacion() {
        Incidencia incidencia = incidenciaEnDesarrollo();
        incidencia.cambiarEstado(EstadoIncidencia.EN_VALIDACION);
        return incidencia;
    }

    private static Incidencia incidenciaFinalizada() {
        Incidencia incidencia = incidenciaEnValidacion();
        incidencia.registrarSolucion("Se reinicio el servicio de red afectado");
        incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA);
        return incidencia;
    }
}
