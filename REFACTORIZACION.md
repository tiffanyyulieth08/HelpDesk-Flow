## Estado anterior

### Problema identificado

La entidad `Incidencia` contiene dentro de `cambiarEstado()` tanto las reglas de
validación como la modificación efectiva del estado y el registro de la fecha
de cierre.

Además, `IncidenciaConsultaService` conoce directamente que la prioridad se
calcula mediante impacto y urgencia, aunque la entidad ya ofrece
`getPrioridad()`.

### Evidencia

- `Incidencia.cambiarEstado()` valida la secuencia, exige una solución,
  modifica el estado y genera la fecha de cierre.
- No existe una clase `ValidadorTransicion`.
- `IncidenciaConsultaService.filtrarPorPrioridad()` llama directamente a
  `CalculadoraPrioridad`.

### Consecuencia

- La entidad concentra validación y mutación.
- La forma de obtener la prioridad está conocida por más de un componente.
- Las responsabilidades son menos claras.
- Cambiar una regla de transición obliga a modificar directamente la entidad.

### Comportamiento que debe preservarse

- No se permiten saltos de estado.
- No se permiten retrocesos.
- Solo se puede finalizar con una solución.
- La fecha de cierre se asigna al finalizar.
- La regla EXPEDITE continúa funcionando.
- Los filtros por prioridad producen los mismos resultados.

### Línea base

- Comando: `mvn clean verify`
- Pruebas: 64
- Fallos: 0
- Errores: 0
- Resultado: `BUILD SUCCESS`