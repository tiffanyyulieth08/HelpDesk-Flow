package cr.ac.utn.helpdeskflow.domain;

import cr.ac.utn.helpdeskflow.exception.ReglaNegocioException;

public final class ValidadorTransicion {

    private ValidadorTransicion() {
    }

    public static void validar(EstadoIncidencia estadoActual, EstadoIncidencia nuevoEstado, String solucionAplicada) {
        if (nuevoEstado == null || nuevoEstado.ordinal() != estadoActual.ordinal() + 1) {
            throw new ReglaNegocioException("Solo se permite avanzar al estado siguiente");
        }
        if (nuevoEstado == EstadoIncidencia.FINALIZADA
                && (solucionAplicada == null || solucionAplicada.isBlank())) {
            throw new ReglaNegocioException("No se puede finalizar sin una solucion registrada");
        }
    }
}
