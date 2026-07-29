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

## Cambio realizado

### Ciclo 1 — Extracción de la validación de transiciones

Se creó `ValidadorTransicion`, responsable de:

- validar que una transición sea secuencial;
- validar que exista solución antes de finalizar.

`Incidencia` conserva:

- la modificación del estado;
- la fecha de cierre;
- sus datos internos.

### Ciclo 2 — Encapsulación del acceso a prioridad

`IncidenciaConsultaService` dejó de conocer que la prioridad depende de impacto
y urgencia.

Ahora utiliza:

`incidencia.getPrioridad()`

La fórmula continúa centralizada en `CalculadoraPrioridad`.

## Comparación

### Antes

- `Incidencia.cambiarEstado()` validaba y mutaba.
- `IncidenciaConsultaService` conocía cómo calcular prioridad.

### Después

- `ValidadorTransicion` valida.
- `Incidencia` cambia el estado.
- `CalculadoraPrioridad` calcula.
- `Incidencia` expone su prioridad.
- `IncidenciaConsultaService` consulta la prioridad de la entidad.

## Pruebas protectoras

- `IncidenciaTransicionTest`
- `IncidenciaTest`
- `ExpediteWorkflowTest`
- `IncidenciaConsultaServiceTest`
- `ConsultaIncidenciasFuncionalTest`
- `CalculadoraPrioridadTest`

## Resultado obtenido

- Línea base de la refactorización: 64 pruebas.
- Verificación de entrega final: 69 pruebas.
- Fallos: 0
- Errores: 0
- Resultado: `BUILD SUCCESS`
- Comportamiento observable: sin cambios

## Participación Ping-Pong

### Ciclo 1

- Driver: Tiffany
- Navigator: Seidy
- Cambio: extracción de `ValidadorTransicion`.

### Ciclo 2

- Driver: Seidy
- Navigator: Tiffany
- Cambio: delegación de la prioridad en la entidad.
