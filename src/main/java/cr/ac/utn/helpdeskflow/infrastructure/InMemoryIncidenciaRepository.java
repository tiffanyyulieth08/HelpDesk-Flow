package cr.ac.utn.helpdeskflow.infrastructure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class InMemoryIncidenciaRepository implements IncidenciaRepository {

    private final Map<UUID, Incidencia> incidencias = new LinkedHashMap<>();

    @Override
    public void guardar(Incidencia incidencia) {
        incidencias.put(incidencia.getId(), incidencia);
    }

    @Override
    public List<Incidencia> buscarTodas() {
        return List.copyOf(incidencias.values());
    }

    @Override
    public Optional<Incidencia> buscarPorId(UUID id) {
        return Optional.ofNullable(incidencias.get(id));
    }
}
