package cr.ac.utn.helpdeskflow.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import cr.ac.utn.helpdeskflow.domain.EstadoIncidencia;
import cr.ac.utn.helpdeskflow.domain.ClaseServicio;
import cr.ac.utn.helpdeskflow.domain.Impacto;
import cr.ac.utn.helpdeskflow.domain.Incidencia;
import cr.ac.utn.helpdeskflow.domain.Urgencia;

class IncidenciaJdbcMapper {

    static final String SELECT_COLUMNAS = "id, titulo, descripcion, categoria, impacto, urgencia, "
            + "estado, clase_servicio, fecha_creacion, fecha_cierre, solucion_aplicada";

    Incidencia mapear(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String titulo = rs.getString("titulo");
        String descripcion = rs.getString("descripcion");
        String categoria = rs.getString("categoria");
        Impacto impacto = Impacto.valueOf(rs.getString("impacto"));
        Urgencia urgencia = Urgencia.valueOf(rs.getString("urgencia"));
        EstadoIncidencia estado = EstadoIncidencia.valueOf(rs.getString("estado"));
        ClaseServicio claseServicio = ClaseServicio.valueOf(rs.getString("clase_servicio"));
        LocalDateTime fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();
        LocalDateTime fechaCierre = obtenerNullableTimestamp(rs, "fecha_cierre");
        String solucionAplicada = rs.getString("solucion_aplicada");
        return Incidencia.rehidratar(id, titulo, descripcion, categoria, impacto, urgencia,
                estado, solucionAplicada, fechaCreacion, fechaCierre, claseServicio);
    }

    static LocalDateTime obtenerNullableTimestamp(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
