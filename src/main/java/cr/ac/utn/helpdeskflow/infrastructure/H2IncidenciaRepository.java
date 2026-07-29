package cr.ac.utn.helpdeskflow.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Urgencia;
import cr.ac.utn.helpdeskflow.repository.IncidenciaRepository;

public class H2IncidenciaRepository implements IncidenciaRepository {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public H2IncidenciaRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS incidencias ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "titulo VARCHAR(255) NOT NULL, "
                + "descripcion VARCHAR(1000) NOT NULL, "
                + "categoria VARCHAR(100) NOT NULL, "
                + "impacto VARCHAR(20) NOT NULL, "
                + "urgencia VARCHAR(20) NOT NULL, "
                + "estado VARCHAR(30) NOT NULL, "
                + "fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "fecha_cierre TIMESTAMP, "
                + "solucion_aplicada VARCHAR(2000)"
                + ")";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la tabla incidencias", e);
        }
    }

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public void guardar(Incidencia incidencia) {
        if (existe(incidencia.getId())) {
            actualizar(incidencia);
        } else {
            insertar(incidencia);
        }
    }

    private boolean existe(UUID id) {
        String sql = "SELECT COUNT(*) FROM incidencias WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de incidencia", e);
        }
    }

    private void insertar(Incidencia incidencia) {
        String sql = "INSERT INTO incidencias (id, titulo, descripcion, categoria, impacto, urgencia, estado, fecha_cierre, solucion_aplicada) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, incidencia.getId().toString());
            stmt.setString(2, incidencia.getTitulo());
            stmt.setString(3, incidencia.getDescripcion());
            stmt.setString(4, incidencia.getCategoria());
            stmt.setString(5, incidencia.getImpacto().name());
            stmt.setString(6, incidencia.getUrgencia().name());
            stmt.setString(7, incidencia.getEstado().name());
            setNullableTimestamp(stmt, 8, incidencia.getFechaCierre());
            stmt.setString(9, incidencia.getSolucionAplicada());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar incidencia", e);
        }
    }

    private void actualizar(Incidencia incidencia) {
        String sql = "UPDATE incidencias SET titulo = ?, descripcion = ?, categoria = ?, impacto = ?, "
                + "urgencia = ?, estado = ?, fecha_cierre = ?, solucion_aplicada = ? WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, incidencia.getTitulo());
            stmt.setString(2, incidencia.getDescripcion());
            stmt.setString(3, incidencia.getCategoria());
            stmt.setString(4, incidencia.getImpacto().name());
            stmt.setString(5, incidencia.getUrgencia().name());
            stmt.setString(6, incidencia.getEstado().name());
            setNullableTimestamp(stmt, 7, incidencia.getFechaCierre());
            stmt.setString(8, incidencia.getSolucionAplicada());
            stmt.setString(9, incidencia.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar incidencia", e);
        }
    }

    @Override
    public Optional<Incidencia> buscarPorId(UUID id) {
        String sql = "SELECT id, titulo, descripcion, categoria, impacto, urgencia, estado, "
                + "fecha_cierre, solucion_aplicada FROM incidencias WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearIncidencia(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar incidencia por id", e);
        }
    }

    @Override
    public List<Incidencia> buscarTodas() {
        String sql = "SELECT id, titulo, descripcion, categoria, impacto, urgencia, estado, "
                + "fecha_cierre, solucion_aplicada FROM incidencias ORDER BY fecha_creacion, id";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Incidencia> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapearIncidencia(rs));
            }
            return Collections.unmodifiableList(resultado);
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar incidencias", e);
        }
    }

    private Incidencia mapearIncidencia(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String titulo = rs.getString("titulo");
        String descripcion = rs.getString("descripcion");
        String categoria = rs.getString("categoria");
        Impacto impacto = Impacto.valueOf(rs.getString("impacto"));
        Urgencia urgencia = Urgencia.valueOf(rs.getString("urgencia"));
        EstadoIncidencia estado = EstadoIncidencia.valueOf(rs.getString("estado"));
        LocalDateTime fechaCierre = obtenerNullableTimestamp(rs, "fecha_cierre");
        String solucionAplicada = rs.getString("solucion_aplicada");
        return Incidencia.rehidratar(id, titulo, descripcion, categoria, impacto, urgencia,
                estado, solucionAplicada, fechaCierre);
    }

    private void setNullableTimestamp(PreparedStatement stmt, int index, LocalDateTime value) throws SQLException {
        if (value != null) {
            stmt.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            stmt.setNull(index, java.sql.Types.TIMESTAMP);
        }
    }

    private LocalDateTime obtenerNullableTimestamp(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
