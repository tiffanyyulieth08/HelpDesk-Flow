package cr.ac.utn.helpdeskflow;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.application.IncidenciaConsultaService;
import cr.ac.utn.helpdeskflow.application.IncidenciaWorkflowService;
import cr.ac.utn.helpdeskflow.application.MetricasResumen;
import cr.ac.utn.helpdeskflow.application.MetricasService;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.infrastructure.H2IncidenciaRepository;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class App {

    public static void main(String[] args) {
        IncidenciaRepository repository = new H2IncidenciaRepository(
                "jdbc:h2:file:./data/helpdesk-flow", "sa", "");
        new App(new Scanner(System.in), System.out, repository).ejecutar();
    }

    private final Scanner entrada;
    private final PrintStream salida;
    private final IncidenciaRepository repository;
    private final IncidenciaConsultaService consultas;
    private final IncidenciaWorkflowService workflow;
    private final MetricasService metricas;

    public App(Scanner entrada, PrintStream salida, IncidenciaRepository repository) {
        this.entrada = entrada;
        this.salida = salida;
        this.repository = repository;
        this.consultas = new IncidenciaConsultaService(repository);
        this.workflow = new IncidenciaWorkflowService(repository);
        this.metricas = new MetricasService(repository);
    }

    public void ejecutar() {
        boolean continuar = true;
        while (continuar && entrada.hasNextLine()) {
            mostrarMenu();
            String opcion = entrada.nextLine().trim();
            if (opcion.isBlank()) {
                continue;
            }
            try {
                continuar = ejecutarOpcion(opcion);
            } catch (ReglaNegocioException excepcion) {
                salida.println("Regla de negocio: " + excepcion.getMessage());
            }
        }
        if (!continuar) {
            salida.println("Hasta luego.");
        }
    }

    private boolean ejecutarOpcion(String opcion) {
        switch (opcion) {
            case "1" -> registrarIncidencia();
            case "2" -> mostrarIncidencias(consultas.listarTodas());
            case "3" -> mostrarIncidencia(consultas.buscarPorId(leerUuid()));
            case "4" -> mostrarIncidencias(consultas.filtrarPorEstado(leerEnum(EstadoIncidencia.class, "estado")));
            case "5" -> mostrarIncidencias(consultas.filtrarPorPrioridad(leerEnum(Prioridad.class, "prioridad")));
            case "6" -> mostrarIncidencias(consultas.listarAbiertas());
            case "7" -> mostrarIncidencias(consultas.listarFinalizadas());
            case "8" -> cambiarEstado();
            case "9" -> registrarSolucion();
            case "10" -> marcarExpedite();
            case "11" -> mostrarMetricas();
            case "0" -> { return false; }
            default -> salida.println("Opcion invalida. Seleccione un numero del 0 al 11.");
        }
        return true;
    }

    private void mostrarMenu() {
        salida.println("\n=== HelpDesk Flow ===");
        salida.println("1. Registrar incidencia");
        salida.println("2. Listar incidencias");
        salida.println("3. Buscar por identificador");
        salida.println("4. Filtrar por estado");
        salida.println("5. Filtrar por prioridad");
        salida.println("6. Mostrar abiertas");
        salida.println("7. Mostrar finalizadas");
        salida.println("8. Cambiar el estado de una incidencia");
        salida.println("9. Registrar la solucion aplicada");
        salida.println("10. Marcar una incidencia critica como EXPEDITE");
        salida.println("11. Mostrar metricas");
        salida.println("0. Salir");
        solicitar("Seleccione una opcion: ");
    }

    private void registrarIncidencia() {
        String titulo = leerTitulo();
        String descripcion = leerDescripcion();
        String categoria = leerTextoObligatorio("Categoria: ");
        Impacto impacto = leerEnum(Impacto.class, "impacto");
        Urgencia urgencia = leerEnum(Urgencia.class, "urgencia");
        Incidencia incidencia = Incidencia.crear(titulo, descripcion, categoria, impacto, urgencia);
        repository.guardar(incidencia);
        salida.println("Incidencia registrada correctamente.");
        mostrarIncidencia(incidencia);
    }

    private void cambiarEstado() {
        UUID id = leerUuid();
        EstadoIncidencia estado = leerEnum(EstadoIncidencia.class, "estado nuevo");
        workflow.cambiarEstado(id, estado);
        salida.println("Estado actualizado correctamente.");
    }

    private void registrarSolucion() {
        UUID id = leerUuid();
        String solucion = leerTextoObligatorio("Solucion aplicada: ");
        workflow.registrarSolucion(id, solucion);
        salida.println("Solucion registrada correctamente.");
    }

    private void marcarExpedite() {
        salida.println("Solo las incidencias con prioridad CRITICA pueden ser EXPEDITE "
                + "(impacto ALTO y urgencia ALTA).");
        workflow.marcarComoExpedite(leerUuid());
        salida.println("Incidencia marcada como EXPEDITE.");
    }

    private void mostrarMetricas() {
        MetricasResumen resumen = metricas.calcular();
        salida.println("\n--- Metricas ---");
        salida.println("Total: " + resumen.getTotal());
        salida.println("Abiertas: " + resumen.getAbiertas());
        salida.println("Finalizadas: " + resumen.getFinalizadas());
        salida.println("Throughput: " + resumen.getThroughput());
        salida.println("Lead time promedio: " + formatearDuracion(resumen.getLeadTimePromedio()));
        salida.println("Por prioridad: " + resumen.getCantidadPorPrioridad());
    }

    private void mostrarIncidencias(List<Incidencia> incidencias) {
        if (incidencias.isEmpty()) {
            salida.println("No se encontraron incidencias.");
            return;
        }
        incidencias.forEach(this::mostrarIncidencia);
    }

    private void mostrarIncidencia(Incidencia incidencia) {
        salida.println("------------------------------");
        salida.println("ID: " + incidencia.getId());
        salida.println("Titulo: " + incidencia.getTitulo());
        salida.println("Descripcion: " + incidencia.getDescripcion());
        salida.println("Categoria: " + incidencia.getCategoria());
        salida.println("Impacto: " + incidencia.getImpacto());
        salida.println("Urgencia: " + incidencia.getUrgencia());
        salida.println("Prioridad: " + incidencia.getPrioridad());
        salida.println("Estado: " + incidencia.getEstado());
        salida.println("Clase de servicio: " + incidencia.getClaseServicio());
        salida.println("Fecha de creacion: " + incidencia.getFechaCreacion());
        salida.println("Fecha de cierre: " + incidencia.getFechaCierre());
        salida.println("Solucion aplicada: " + incidencia.getSolucionAplicada());
    }

    private String leerTextoObligatorio(String mensaje) {
        while (entrada.hasNextLine()) {
            solicitar(mensaje);
            String valor = entrada.nextLine().trim();
            if (!valor.isBlank()) {
                return valor;
            }
            salida.println("Este campo es obligatorio. Intente nuevamente.");
        }
        return "";
    }

    private String leerTitulo() {
        while (entrada.hasNextLine()) {
            solicitar("Titulo: ");
            String valor = entrada.nextLine().trim();
            if (valor.isBlank()) {
                salida.println("Este campo es obligatorio. Intente nuevamente.");
            } else if (esOpcionDelMenu(valor)) {
                salida.println("El titulo no puede ser una opcion del menu. "
                        + "Escriba el titulo de la incidencia e intente nuevamente.");
            } else {
                return valor;
            }
        }
        return "";
    }

    private boolean esOpcionDelMenu(String valor) {
        try {
            int opcion = Integer.parseInt(valor);
            return opcion >= 0 && opcion <= 11;
        } catch (NumberFormatException excepcion) {
            return false;
        }
    }

    private String leerDescripcion() {
        while (entrada.hasNextLine()) {
            solicitar("Descripcion: ");
            String valor = entrada.nextLine().trim();
            if (valor.length() >= 10) {
                return valor;
            }
            salida.println("La descripcion debe contener al menos 10 caracteres. Intente nuevamente.");
        }
        return "";
    }

    private UUID leerUuid() {
        while (entrada.hasNextLine()) {
            solicitar("Identificador (UUID): ");
            String valor = entrada.nextLine().trim();
            try {
                return UUID.fromString(valor);
            } catch (IllegalArgumentException excepcion) {
                salida.println("Identificador invalido. Debe tener formato UUID. Intente nuevamente.");
            }
        }
        throw new ReglaNegocioException("No se recibio un identificador");
    }

    private <T extends Enum<T>> T leerEnum(Class<T> tipo, String nombre) {
        String valoresPermitidos = String.join(", ", nombres(tipo));
        while (entrada.hasNextLine()) {
            solicitar("Ingrese " + nombre + " (" + valoresPermitidos + "): ");
            String valor = entrada.nextLine().trim();
            try {
                return Enum.valueOf(tipo, valor.toUpperCase());
            } catch (IllegalArgumentException excepcion) {
                salida.println("Valor invalido. " + nombre + " debe ser uno de: "
                        + valoresPermitidos + ". Intente nuevamente.");
            }
        }
        throw new ReglaNegocioException("No se recibio un valor para " + nombre);
    }

    private <T extends Enum<T>> List<String> nombres(Class<T> tipo) {
        return java.util.Arrays.stream(tipo.getEnumConstants()).map(Enum::name).toList();
    }

    private void solicitar(String mensaje) {
        salida.print(mensaje);
        salida.flush();
    }

    private String formatearDuracion(Duration duracion) {
        return duracion.isZero() ? "0" : duracion.toString();
    }
}
