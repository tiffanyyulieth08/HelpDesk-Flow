package cr.ac.utn.helpdeskflow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class IncidenciaTest {

    @Test
    void rechazaTituloNulo() {
        assertThrows(ReglaNegocioException.class,
                () -> Incidencia.crear(null, "Descripcion valida con mas de diez caracteres", "Hardware", Impacto.MEDIO, Urgencia.MEDIA));
    }

    @Test
    void rechazaTituloVacio() {
        assertThrows(ReglaNegocioException.class,
                () -> Incidencia.crear("", "Descripcion valida con mas de diez caracteres", "Hardware", Impacto.MEDIO, Urgencia.MEDIA));
    }

    @Test
    void rechazaTituloConSoloEspacios() {
        assertThrows(ReglaNegocioException.class,
                () -> Incidencia.crear("   ", "Descripcion valida con mas de diez caracteres", "Hardware", Impacto.MEDIO, Urgencia.MEDIA));
    }

    @Test
    void rechazaDescripcionConMenosDeDiezCaracteres() {
        assertThrows(ReglaNegocioException.class,
                () -> Incidencia.crear("Titulo valido", "Corta", "Hardware", Impacto.MEDIO, Urgencia.MEDIA));
    }

    @Test
    void aceptaDescripcionConExactamenteDiezCaracteres() {
        Incidencia incidencia = Incidencia.crear("Titulo valido", "1234567890", "Hardware", Impacto.MEDIO, Urgencia.MEDIA);
        assertEquals("1234567890", incidencia.getDescripcion());
    }

    @Test
    void generaIdentificadorUnico() {
        Incidencia primera = Incidencia.crear("Titulo uno", "Descripcion valida con mas de diez caracteres", "Hardware", Impacto.BAJO, Urgencia.BAJA);
        Incidencia segunda = Incidencia.crear("Titulo dos", "Otra descripcion valida con mas de diez caracteres", "Software", Impacto.ALTO, Urgencia.ALTA);

        assertNotNull(primera.getId());
        assertNotNull(segunda.getId());
        assertNotEquals(primera.getId(), segunda.getId());
    }

    @Test
    void iniciaConEstadoRegistrada() {
        Incidencia incidencia = Incidencia.crear("Titulo valido", "Descripcion valida con mas de diez caracteres", "Redes", Impacto.MEDIO, Urgencia.MEDIA);
        assertEquals(EstadoIncidencia.REGISTRADA, incidencia.getEstado());
    }
}
