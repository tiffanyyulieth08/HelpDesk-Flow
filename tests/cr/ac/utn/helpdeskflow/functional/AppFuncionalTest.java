package cr.ac.utn.helpdeskflow.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.UUID;

import cr.ac.utn.helpdeskflow.App;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.infrastructure.H2IncidenciaRepository;
import cr.ac.utn.helpdeskflow.infrastructure.InMemoryIncidenciaRepository;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;
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

    @Test
    void cadaPromptApareceAntesDeLaEntrada() throws Exception {
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        pipeIn.connect(pipeOut);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        App app = new App(
                new Scanner(pipeIn),
                salida,
                new InMemoryIncidenciaRepository());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(app::ejecutar);

        esperarPrompt(bytes, "Seleccione una opcion:", 3000);
        String salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Seleccione una opcion:"),
                "El menu debe aparecer antes de escribir una opcion");

        pipeOut.write("1\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Titulo:", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Titulo:"),
                "Titulo: debe aparecer antes de escribir el titulo");

        pipeOut.write("Servidor sin acceso\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Descripcion:", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Descripcion:"),
                "Descripcion: debe aparecer despues del titulo");

        pipeOut.write("La red principal no responde desde esta manana\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Categoria:", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Categoria:"),
                "Categoria: debe aparecer despues de la descripcion");

        pipeOut.write("Infraestructura\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Ingrese impacto", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Ingrese impacto"),
                "Impacto debe aparecer despues de categoria");

        pipeOut.write("ALTO\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Ingrese urgencia", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Ingrese urgencia"),
                "Urgencia debe aparecer despues de impacto");

        pipeOut.write("ALTA\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Incidencia registrada", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Incidencia registrada correctamente."));

        pipeOut.write("0\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Hasta luego.", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Hasta luego."));

        executor.shutdownNow();
    }

    @Test
    void noSeMuestraErrorDeTituloAntesDeProporcionarValorInvalido() throws Exception {
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        pipeIn.connect(pipeOut);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        App app = new App(
                new Scanner(pipeIn),
                salida,
                new InMemoryIncidenciaRepository());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(app::ejecutar);

        esperarPrompt(bytes, "Seleccione una opcion:", 3000);
        pipeOut.write("1\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Titulo:", 3000);
        String salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertFalse(salidaActual.contains("opcion del menu"),
                "No debe mostrar advertencia de opcion del menu antes de escribir");

        pipeOut.write("   \n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Este campo es obligatorio", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Este campo es obligatorio"),
                "Debe mostrar error de titulo solo despues de ingresar valor invalido");

        pipeOut.write("Servidor sin acceso\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Descripcion:", 3000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Descripcion:"));

        pipeOut.write("La red principal no responde desde esta manana\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Categoria:", 3000);
        pipeOut.write("Infraestructura\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Ingrese impacto", 3000);
        pipeOut.write("ALTO\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Ingrese urgencia", 3000);
        pipeOut.write("ALTA\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Incidencia registrada", 3000);

        pipeOut.write("0\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Hasta luego.", 3000);

        executor.shutdownNow();
    }

    @Test
    void impactoInvalidoSolicitaNuevamenteSoloImpacto() throws Exception {
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        pipeIn.connect(pipeOut);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        App app = new App(
                new Scanner(pipeIn),
                salida,
                new InMemoryIncidenciaRepository());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(app::ejecutar);

        esperarPrompt(bytes, "Seleccione una opcion:", 2000);
        pipeOut.write("1\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Titulo:", 2000);
        pipeOut.write("Servidor sin acceso\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Descripcion:", 2000);
        pipeOut.write("La red principal no funciona desde esta manana\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Categoria:", 2000);
        pipeOut.write("Infraestructura\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Ingrese impacto", 2000);
        String salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Ingrese impacto"),
                "Impacto debe aparecer antes de escribir valor");
        assertFalse(salidaActual.contains("Valor invalido"),
                "No debe mostrar error antes de ingresar impacto invalido");

        pipeOut.write("incorrecto\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Valor invalido", 2000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Valor invalido"),
                "Debe mostrar error solo despues de impacto invalido");
        assertTrue(salidaActual.contains("Ingrese impacto"),
                "Debe solicitar nuevamente el impacto");

        pipeOut.write("ALTO\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Ingrese urgencia", 2000);
        pipeOut.write("ALTA\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Incidencia registrada", 2000);
        pipeOut.write("0\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Hasta luego.", 2000);

        executor.shutdownNow();
    }

    @Test
    void urgenciaInvalidaSolicitaNuevamenteSoloUrgencia() throws Exception {
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        pipeIn.connect(pipeOut);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        App app = new App(
                new Scanner(pipeIn),
                salida,
                new InMemoryIncidenciaRepository());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(app::ejecutar);

        esperarPrompt(bytes, "Seleccione una opcion:", 2000);
        pipeOut.write("1\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Titulo:", 2000);
        pipeOut.write("Correo electronico lento\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Descripcion:", 2000);
        pipeOut.write("El servicio de correo presenta demoras desde ayer\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Categoria:", 2000);
        pipeOut.write("Sistemas\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Ingrese impacto", 2000);
        pipeOut.write("MEDIO\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Ingrese urgencia", 2000);
        String salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Ingrese urgencia"),
                "Urgencia debe aparecer antes de escribir valor");
        assertFalse(salidaActual.contains("Valor invalido"),
                "No debe mostrar error antes de ingresar urgencia invalida");

        pipeOut.write("incorrecta\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Valor invalido", 2000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Valor invalido"),
                "Debe mostrar error solo despues de urgencia invalida");
        assertTrue(salidaActual.contains("Ingrese urgencia"),
                "Debe solicitar nuevamente la urgencia");

        pipeOut.write("ALTA\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Incidencia registrada", 2000);
        pipeOut.write("0\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Hasta luego.", 2000);

        executor.shutdownNow();
    }

    @Test
    void opcionInvalidaNoDesplazaEntradas() throws Exception {
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        pipeIn.connect(pipeOut);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        App app = new App(
                new Scanner(pipeIn),
                salida,
                new InMemoryIncidenciaRepository());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(app::ejecutar);

        esperarPrompt(bytes, "Seleccione una opcion:", 2000);
        pipeOut.write("99\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Opcion invalida", 2000);
        String salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Opcion invalida"),
                "Debe mostrar mensaje de opcion invalida");

        esperarPrompt(bytes, "Seleccione una opcion:", 2000);
        pipeOut.write("1\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Titulo:", 2000);
        pipeOut.write("Servidor caido\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Descripcion:", 2000);
        pipeOut.write("El servidor principal no responde desde las 8am\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Categoria:", 2000);
        pipeOut.write("Infraestructura\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Ingrese impacto", 2000);
        pipeOut.write("ALTO\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Ingrese urgencia", 2000);
        pipeOut.write("ALTA\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Incidencia registrada", 2000);

        pipeOut.write("0\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Hasta luego.", 2000);

        executor.shutdownNow();
    }

    @Test
    void opcionCeroFinalizaCorrectamente() {
        String comandos = "0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new H2IncidenciaRepository(
                        "jdbc:h2:mem:app-cero;DB_CLOSE_DELAY=-1", "sa", ""));

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertEquals(1, contar(salida, "=== HelpDesk Flow ==="));
        assertTrue(salida.contains("Hasta luego."));
        assertFalse(salida.contains("Opcion invalida"));
    }

    @Test
    void entradaVaciaEnMenuMuestraMensaje() {
        String comandos = "\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new H2IncidenciaRepository(
                        "jdbc:h2:mem:app-vacia;DB_CLOSE_DELAY=-1", "sa", ""));

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("Debe seleccionar una opcion."),
                "Debe mostrar mensaje de opcion vacia");
        assertEquals(2, contar(salida, "=== HelpDesk Flow ==="),
                "Debe volver al menu exactamente una vez mas");
    }

    @Test
    void vuelveAlMenuUnaSolaVezDespuesDeOperacion() {
        String comandos = String.join("\n",
                "1",
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
                        "jdbc:h2:mem:app-vuelta;DB_CLOSE_DELAY=-1", "sa", ""));

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertEquals(2, contar(salida, "=== HelpDesk Flow ==="),
                "Menu debe aparecer exactamente 2 veces (inicio + despues de operacion)");
    }

    @Test
    void flujoCompletoRegistroSolucionCambioEstadosYFinalizacion() {
        IncidenciaRepository repo = new InMemoryIncidenciaRepository();
        Incidencia incidencia = Incidencia.crear(
                "Servidor principal caido",
                "El servidor de base de datos no responde desde esta manana temprano",
                "Infraestructura",
                Impacto.ALTO,
                Urgencia.ALTA);
        repo.guardar(incidencia);
        UUID id = incidencia.getId();

        String comandos = String.join("\n",
                "2",
                "9",
                id.toString(),
                "Se reinicio el servidor y se restauro la conexion",
                "8",
                id.toString(),
                "LISTA",
                "8",
                id.toString(),
                "EN_DESARROLLO",
                "8",
                id.toString(),
                "EN_VALIDACION",
                "6",
                "7",
                "0") + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                repo);

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("Solucion registrada correctamente."));
        assertTrue(salida.contains("Estado actualizado correctamente."));
        assertTrue(salida.contains("EN_VALIDACION"));
    }

    @Test
    void flujoExpediteSobreIncidenciaCritica() {
        IncidenciaRepository repo = new InMemoryIncidenciaRepository();
        Incidencia incidencia = Incidencia.crear(
                "Fallo critico de seguridad",
                "Se detecto una vulnerabilidad critica en el sistema de autenticacion",
                "Seguridad",
                Impacto.ALTO,
                Urgencia.ALTA);
        repo.guardar(incidencia);
        UUID id = incidencia.getId();

        String comandos = String.join("\n",
                "10",
                id.toString(),
                "3",
                id.toString(),
                "0") + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                repo);

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("Incidencia marcada como EXPEDITE."));
        assertTrue(salida.contains("EXPEDITE"));
    }

    @Test
    void filtrarPorEstadoAceptaMayusculasYMinusculas() throws Exception {
        IncidenciaRepository repo = new InMemoryIncidenciaRepository();
        Incidencia incidencia = Incidencia.crear(
                "Problema de red",
                "El switch principal del edificio esta presentando fallas intermitentes",
                "Redes",
                Impacto.MEDIO,
                Urgencia.MEDIA);
        repo.guardar(incidencia);

        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        pipeIn.connect(pipeOut);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        App app = new App(new Scanner(pipeIn), salida, repo);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(app::ejecutar);

        esperarPrompt(bytes, "Seleccione una opcion:", 2000);
        pipeOut.write("4\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Ingrese estado", 2000);
        pipeOut.write("registrada\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "No se encontraron", 3000);
        String salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertFalse(salidaActual.contains("Valor invalido"),
                "Debe aceptar estado en minusculas sin mostrar error");

        pipeOut.write("0\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Hasta luego.", 2000);
        executor.shutdownNow();
    }

    @Test
    void buscarPorIdentificadorInvalidoSolicitaNuevamente() throws Exception {
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream();
        pipeIn.connect(pipeOut);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        App app = new App(
                new Scanner(pipeIn),
                salida,
                new InMemoryIncidenciaRepository());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(app::ejecutar);

        esperarPrompt(bytes, "Seleccione una opcion:", 2000);
        pipeOut.write("3\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Identificador (UUID):", 2000);
        String salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Identificador (UUID):"),
                "El prompt del UUID debe aparecer antes de escribir");

        pipeOut.write("no-es-un-uuid\n".getBytes());
        pipeOut.flush();

        esperarPrompt(bytes, "Identificador invalido", 2000);
        salidaActual = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salidaActual.contains("Identificador invalido"),
                "Debe mostrar error de UUID invalido");

        pipeOut.write("0\n".getBytes());
        pipeOut.flush();
        esperarPrompt(bytes, "Hasta luego.", 2000);

        executor.shutdownNow();
    }

    @Test
    void opcionesSinEntradasAdicionalesFuncionanCorrectamente() {
        String comandos = String.join("\n",
                "11",
                "6",
                "7",
                "0") + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App app = new App(
                new Scanner(comandos),
                new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new InMemoryIncidenciaRepository());

        app.ejecutar();

        String salida = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(salida.contains("--- Metricas ---"));
        assertTrue(salida.contains("Total: 0"));
        assertTrue(salida.contains("No se encontraron incidencias."));
        assertEquals(4, contar(salida, "=== HelpDesk Flow ==="),
                "Menu debe aparecer 4 veces (inicio + 3 operaciones)");
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

    private static void esperarPrompt(ByteArrayOutputStream bytes, String texto, int timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (bytes.toString(StandardCharsets.UTF_8).contains(texto)) {
                return;
            }
            Thread.sleep(50);
        }
    }
}
