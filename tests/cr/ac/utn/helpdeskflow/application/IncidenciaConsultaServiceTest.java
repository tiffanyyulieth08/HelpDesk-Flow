package cr.ac.utn.helpdeskflow.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Prioridad;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;
import cr.ac.utn.helpdeskflow.infrastructure.InMemoryIncidenciaRepository;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncidenciaConsultaServiceTest {

    private IncidenciaRepository repository;
    private IncidenciaConsultaService service;

    @BeforeEach
    void configurar() {
        repository = new InMemoryIncidenciaRepository();
        service = new IncidenciaConsultaService(repository);
    }

    @Test
    void listaTodasLasIncidencias() {
        Incidencia primera = crearIncidencia("Falla de red", Impacto.ALTO, Urgencia.ALTA);
        Incidencia segunda = crearIncidencia("Error de correo", Impacto.MEDIO, Urgencia.BAJA);
        repository.guardar(primera);
        repository.guardar(segunda);

        List<Incidencia> resultado = service.listarTodas();

        assertEquals(Set.of(primera, segunda), Set.copyOf(resultado));
    }

    @Test
    void buscaIncidenciaExistentePorIdentificador() {
        Incidencia incidencia = crearIncidencia("Impresora sin conexion", Impacto.BAJO, Urgencia.MEDIA);
        repository.guardar(incidencia);

        Incidencia resultado = service.buscarPorId(incidencia.getId());

        assertSame(incidencia, resultado);
    }

    @Test
    void rechazaIdentificadorInexistente() {
        UUID identificadorInexistente = UUID.randomUUID();

        assertThrows(ReglaNegocioException.class,
                () -> service.buscarPorId(identificadorInexistente));
    }

    @Test
    void filtraIncidenciasPorEstado() {
        Incidencia registrada = crearIncidencia("VPN inestable", Impacto.MEDIO, Urgencia.MEDIA);
        Incidencia lista = crearIncidencia("Monitor defectuoso", Impacto.BAJO, Urgencia.BAJA);
        lista.cambiarEstado(EstadoIncidencia.LISTA);
        repository.guardar(registrada);
        repository.guardar(lista);

        List<Incidencia> resultado = service.filtrarPorEstado(EstadoIncidencia.LISTA);

        assertEquals(List.of(lista), resultado);
        assertTrue(resultado.stream()
                .allMatch(incidencia -> incidencia.getEstado() == EstadoIncidencia.LISTA));
    }

    @Test
    void filtraIncidenciasPorPrioridad() {
        Incidencia critica = crearIncidencia("Servidor fuera de linea", Impacto.ALTO, Urgencia.ALTA);
        Incidencia alta = crearIncidencia("Aplicacion lenta", Impacto.ALTO, Urgencia.MEDIA);
        Incidencia normal = crearIncidencia("Cambio de teclado", Impacto.BAJO, Urgencia.BAJA);
        repository.guardar(critica);
        repository.guardar(alta);
        repository.guardar(normal);

        List<Incidencia> resultado = service.filtrarPorPrioridad(Prioridad.ALTA);

        assertEquals(List.of(alta), resultado);
    }

    @Test
    void listaSolamenteIncidenciasAbiertas() {
        Incidencia registrada = crearIncidencia("Acceso bloqueado", Impacto.MEDIO, Urgencia.ALTA);
        Incidencia enDesarrollo = crearIncidencia("Falla de respaldo", Impacto.ALTO, Urgencia.MEDIA);
        avanzarHasta(enDesarrollo, EstadoIncidencia.EN_DESARROLLO);
        Incidencia finalizada = crearIncidencia("Actualizacion pendiente", Impacto.BAJO, Urgencia.BAJA);
        finalizar(finalizada);
        repository.guardar(registrada);
        repository.guardar(enDesarrollo);
        repository.guardar(finalizada);

        List<Incidencia> resultado = service.listarAbiertas();

        assertEquals(Set.of(registrada, enDesarrollo), Set.copyOf(resultado));
        assertTrue(resultado.stream()
                .allMatch(incidencia -> incidencia.getEstado() != EstadoIncidencia.FINALIZADA));
    }

    @Test
    void listaSolamenteIncidenciasFinalizadas() {
        Incidencia abierta = crearIncidencia("Licencia por renovar", Impacto.MEDIO, Urgencia.BAJA);
        Incidencia finalizada = crearIncidencia("Cable reemplazado", Impacto.BAJO, Urgencia.MEDIA);
        finalizar(finalizada);
        repository.guardar(abierta);
        repository.guardar(finalizada);

        List<Incidencia> resultado = service.listarFinalizadas();

        assertEquals(List.of(finalizada), resultado);
        assertTrue(resultado.stream()
                .allMatch(incidencia -> incidencia.getEstado() == EstadoIncidencia.FINALIZADA));
    }

    @Test
    void retornaListaVaciaCuandoNoHayCoincidencias() {
        repository.guardar(crearIncidencia("Solicitud normal", Impacto.BAJO, Urgencia.BAJA));

        List<Incidencia> resultado = service.filtrarPorPrioridad(Prioridad.CRITICA);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarNoModificaElRepositorio() {
        Incidencia incidencia = crearIncidencia("Equipo sin audio", Impacto.BAJO, Urgencia.MEDIA);
        repository.guardar(incidencia);

        List<Incidencia> resultado = service.listarTodas();

        assertThrows(UnsupportedOperationException.class,
                () -> resultado.add(crearIncidencia("Incidencia externa", Impacto.ALTO, Urgencia.ALTA)));
        assertEquals(List.of(incidencia), repository.buscarTodas());
    }

    private static Incidencia crearIncidencia(String titulo, Impacto impacto, Urgencia urgencia) {
        return Incidencia.crear(
                titulo,
                "Descripcion valida para la incidencia de prueba",
                "Soporte",
                impacto,
                urgencia);
    }

    private static void avanzarHasta(Incidencia incidencia, EstadoIncidencia estadoObjetivo) {
        while (incidencia.getEstado() != estadoObjetivo) {
            EstadoIncidencia estadoSiguiente =
                    EstadoIncidencia.values()[incidencia.getEstado().ordinal() + 1];
            incidencia.cambiarEstado(estadoSiguiente);
        }
    }

    private static void finalizar(Incidencia incidencia) {
        avanzarHasta(incidencia, EstadoIncidencia.EN_VALIDACION);
        incidencia.registrarSolucion("Se aplico una solucion verificada");
        incidencia.cambiarEstado(EstadoIncidencia.FINALIZADA);
    }
}
