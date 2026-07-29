package cr.ac.utn.helpdeskflow.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.CalculadoraPrioridad;
import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.infrastructure.H2IncidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class H2IncidenciaRepositoryTest {

    @TempDir
    Path carpetaTemporal;

    private String jdbcUrl;
    private IncidenciaRepository repository;

    @BeforeEach
    void configurar() {
        jdbcUrl = "jdbc:h2:file:"
                + carpetaTemporal.resolve("incidencias").toAbsolutePath().toString().replace('\\', '/');
        repository = nuevoRepositorio();
    }

    @Test
    void guardaYBuscaIncidenciaPorIdentificador() {
        Incidencia incidencia = crearIncidencia(
                "Servidor sin respuesta", "Infraestructura", Impacto.ALTO, Urgencia.ALTA);
        repository.guardar(incidencia);

        Optional<Incidencia> resultado = repository.buscarPorId(incidencia.getId());

        assertTrue(resultado.isPresent());
        assertEquals(incidencia.getId(), resultado.orElseThrow().getId());
        assertEquals(incidencia.getTitulo(), resultado.orElseThrow().getTitulo());
        assertEquals(incidencia.getDescripcion(), resultado.orElseThrow().getDescripcion());
        assertEquals(incidencia.getFechaCreacion(), resultado.orElseThrow().getFechaCreacion());
        assertNull(resultado.orElseThrow().getFechaCierre());
    }

    @Test
    void listaTodasLasIncidenciasGuardadas() {
        Incidencia primera = crearIncidencia(
                "Falla de conectividad", "Redes", Impacto.MEDIO, Urgencia.ALTA);
        Incidencia segunda = crearIncidencia(
                "Teclado defectuoso", "Hardware", Impacto.BAJO, Urgencia.BAJA);
        repository.guardar(primera);
        repository.guardar(segunda);

        List<Incidencia> resultado = repository.buscarTodas();

        assertEquals(List.of(primera.getId(), segunda.getId()),
                resultado.stream().map(Incidencia::getId).toList());
    }

    @Test
    void retornaAusenciaCuandoElIdentificadorNoExiste() {
        Optional<Incidencia> resultado = repository.buscarPorId(UUID.randomUUID());

        assertTrue(resultado.isEmpty());
    }

    @Test
    void actualizaUnaIncidenciaExistente() {
        Incidencia incidencia = crearIncidencia(
                "Aplicacion no inicia", "Software", Impacto.ALTO, Urgencia.MEDIA);
        repository.guardar(incidencia);
        incidencia.cambiarEstado(EstadoIncidencia.LISTA);

        repository.guardar(incidencia);

        List<Incidencia> almacenadas = repository.buscarTodas();
        assertEquals(1, almacenadas.size());
        assertEquals(incidencia.getId(), almacenadas.get(0).getId());
        assertEquals(EstadoIncidencia.LISTA, almacenadas.get(0).getEstado());
    }

    @Test
    void conservaEstadoSolucionYFechaDeCierre() {
        Incidencia incidencia = crearIncidencia(
                "Base de datos bloqueada", "Software", Impacto.ALTO, Urgencia.ALTA);
        finalizar(incidencia);
        repository.guardar(incidencia);

        Incidencia recuperada = repository.buscarPorId(incidencia.getId()).orElseThrow();

        assertEquals(EstadoIncidencia.FINALIZADA, recuperada.getEstado());
        assertEquals(incidencia.getSolucionAplicada(), recuperada.getSolucionAplicada());
        assertNotNull(recuperada.getFechaCierre());
        assertEquals(incidencia.getFechaCierre(), recuperada.getFechaCierre());
    }

    @Test
    void conservaCategoriaImpactoYUrgencia() {
        Incidencia incidencia = crearIncidencia(
                "Servicio de correo lento", "Comunicaciones", Impacto.MEDIO, Urgencia.ALTA);
        repository.guardar(incidencia);

        Incidencia recuperada = repository.buscarPorId(incidencia.getId()).orElseThrow();

        assertEquals("Comunicaciones", recuperada.getCategoria());
        assertEquals(Impacto.MEDIO, recuperada.getImpacto());
        assertEquals(Urgencia.ALTA, recuperada.getUrgencia());
        assertEquals(Prioridad.ALTA,
                CalculadoraPrioridad.calcular(recuperada.getImpacto(), recuperada.getUrgencia()));
    }

    @Test
    void mantieneLosDatosAlRecrearElRepositorio() {
        Incidencia incidencia = crearIncidencia(
                "VPN intermitente", "Redes", Impacto.MEDIO, Urgencia.MEDIA);
        repository.guardar(incidencia);

        IncidenciaRepository repositorioRecreado = nuevoRepositorio();
        Incidencia recuperada = repositorioRecreado.buscarPorId(incidencia.getId()).orElseThrow();

        assertEquals(incidencia.getId(), recuperada.getId());
        assertEquals(incidencia.getTitulo(), recuperada.getTitulo());
        assertEquals(incidencia.getDescripcion(), recuperada.getDescripcion());
        assertEquals(incidencia.getCategoria(), recuperada.getCategoria());
        assertEquals(incidencia.getEstado(), recuperada.getEstado());
        assertEquals(incidencia.getFechaCreacion(), recuperada.getFechaCreacion());
        assertFalse(repositorioRecreado.buscarTodas().isEmpty());
    }

    @Test
    void noExponeUnaColeccionInternaModificable() {
        Incidencia incidencia = crearIncidencia(
                "Monitor sin imagen", "Hardware", Impacto.BAJO, Urgencia.MEDIA);
        repository.guardar(incidencia);
        List<Incidencia> resultado = repository.buscarTodas();

        assertThrows(UnsupportedOperationException.class,
                () -> resultado.add(crearIncidencia(
                        "Elemento externo", "Soporte", Impacto.ALTO, Urgencia.ALTA)));
        assertEquals(1, repository.buscarTodas().size());
    }

    private IncidenciaRepository nuevoRepositorio() {
        return new H2IncidenciaRepository(jdbcUrl, "sa", "");
    }

    private static Incidencia crearIncidencia(
            String titulo,
            String categoria,
            Impacto impacto,
            Urgencia urgencia) {
        return Incidencia.crear(
                titulo,
                "Descripcion suficientemente extensa para la prueba de persistencia",
                categoria,
                impacto,
                urgencia);
    }

    private static void finalizar(Incidencia incidencia) {
        incidencia.cambiarEstado(EstadoIncidencia.LISTA);
        incidencia.cambiarEstado(EstadoIncidencia.EN_DESARROLLO);
        incidencia.cambiarEstado(EstadoIncidencia.EN_VALIDACION);
        incidencia.registrarSolucion("Se aplico y verifico la solucion definitiva");
        incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA);
    }
}
