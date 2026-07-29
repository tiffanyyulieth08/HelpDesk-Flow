package cr.ac.utn.helpdeskflow.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import cr.ac.utn.helpdeskflow.App;
import cr.ac.utn.helpdeskflow.infrastructure.H2IncidenciaRepository;
import org.junit.jupiter.api.Test;

class AppFuncionalTest {

    @Test
    void permiteRegistrarYListarUnaIncidenciaDesdeElMenu() {
        String comandos = String.join("\n",
                "1",
                "Servidor sin acceso",
                "La red principal no responde desde esta manana",
                "Infraestructura",
                "ALTO",
                "ALTA",
                "2",
                "0") + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new H2IncidenciaRepository(
                        "jdbc:h2:mem:app-funcional;DB_CLOSE_DELAY=-1", "sa", ""));

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("Incidencia registrada correctamente."));
        assertTrue(salida.contains("Servidor sin acceso"));
        assertTrue(salida.contains("Prioridad: CRITICA"));
    }

    @Test
    void conservaElOrdenDeLosCamposAlRegistrarUnaIncidencia() {
        String comandos = String.join("\n",
                "1",
                "Cable de red desconectado",
                "Desconectaron el cable de red del rack de Coopelesca",
                "Infraestructura",
                "MEDIO",
                "MEDIA",
                "0") + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new H2IncidenciaRepository(
                        "jdbc:h2:mem:app-campos;DB_CLOSE_DELAY=-1", "sa", ""));

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("Titulo: Cable de red desconectado"));
        assertTrue(salida.contains("Descripcion: Desconectaron el cable de red del rack de Coopelesca"));
        assertTrue(salida.contains("Categoria: Infraestructura"));
        assertTrue(salida.contains("Impacto: MEDIO"));
        assertTrue(salida.contains("Urgencia: MEDIA"));
    }

    @Test
    void solicitaNuevamenteElTituloCuandoEstaEnBlanco() {
        String comandos = String.join("\n",
                "1",
                "   ",
                "Servidor sin acceso",
                "La red principal no responde desde esta manana",
                "Infraestructura",
                "ALTO",
                "ALTA",
                "0") + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new H2IncidenciaRepository(
                        "jdbc:h2:mem:app-titulo;DB_CLOSE_DELAY=-1", "sa", ""));

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("Este campo es obligatorio. Intente nuevamente."));
        assertTrue(salida.contains("Incidencia registrada correctamente."));
    }

    @Test
    void noMuestraElMenuMientrasEjecutaLaOpcionExpedite() {
        String comandos = String.join("\n",
                "10",
                "92b49aaf-781f-4511-a425-7b034c3bda2f",
                "0") + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new H2IncidenciaRepository(
                        "jdbc:h2:mem:app-expedite-menu;DB_CLOSE_DELAY=-1", "sa", ""));

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertEquals(2, contar(salida, "=== HelpDesk Flow ==="));
        assertTrue(salida.indexOf("Identificador (UUID):")
                < salida.lastIndexOf("=== HelpDesk Flow ==="));
    }

    private static int contar(String texto, String fragmento) {
        int cantidad = 0;
        int posicion = 0;
        while ((posicion = texto.indexOf(fragmento, posicion)) >= 0) {
            cantidad++;
            posicion += fragmento.length();
        }
        return cantidad;
    }
}
