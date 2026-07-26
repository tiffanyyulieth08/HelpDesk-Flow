# Tablero Kanban — HelpDesk Flow

## Integrantes

- Tiffany — Desarrollo y revisión
- Seidy — Desarrollo y revisión

## Enlace al tablero

Agregar aquí el enlace del proyecto de GitHub:

`https://github.com/tiffanyyulieth08/HelpDesk-Flow/projects`

## Columnas

El tablero utiliza las siguientes columnas:

1. Opciones / Backlog
2. Preparado
3. En desarrollo
4. Validación
5. Hecho

## Límites WIP

| Columna | Límite |
|---|---:|
| Opciones / Backlog | Sin límite |
| Preparado | 3 |
| En desarrollo | 1 |
| Validación | 1 |
| Hecho | Sin límite |

## Regla de flujo

Cuando una columna alcanza su límite WIP, no se inicia trabajo
nuevo en esa etapa. La prioridad será terminar, revisar,
desbloquear o ayudar a avanzar el trabajo ya iniciado.

## Políticas explícitas

### Entrada a Preparado

Una tarjeta puede pasar a Preparado cuando:

- Tiene una descripción comprensible.
- Tiene criterios de aceptación verificables.
- Su tamaño es manejable.
- Las pruebas principales están identificadas.
- Tiene Driver y Navigator definidos.

### Entrada a En desarrollo

Una tarjeta puede pasar a En desarrollo cuando:

- La columna no ha alcanzado el límite WIP.
- Se creó la rama de trabajo.
- Driver y Navigator conocen los criterios de aceptación.
- El repositorio local está actualizado desde `develop`.

### Entrada a Validación

Una tarjeta puede pasar a Validación cuando:

- El código compila.
- Las pruebas pasan localmente.
- No existen cambios importantes sin confirmar.
- La otra integrante revisó el cambio.
- La rama fue subida a GitHub.
- Existe un Pull Request.

### Entrada a Hecho

Una tarjeta puede pasar a Hecho cuando:

- Los criterios de aceptación fueron verificados.
- La integración continua está en verde.
- El Pull Request fue aprobado e integrado.
- El código está incorporado en `develop` o `main`.
- La documentación fue actualizada.

## Estado inicial

### En desarrollo

- KAN-01 Preparar repositorio y proyecto base.

### Preparado

- KAN-02 HU-01 Registrar una incidencia.
- KAN-03 HU-02 Calcular automáticamente la prioridad.
- KAN-04 HU-03 Gestionar el flujo de estados.

### Opciones / Backlog

- KAN-05 HU-04 Consultar y filtrar incidencias.
- KAN-06 Implementar persistencia H2.
- KAN-07 Incorporar cambio EXPEDITE.
- KAN-08 HU-05 Generar métricas.
- KAN-09 Configurar integración continua.
- KAN-10 Realizar refactorización.
- KAN-11 Completar IA-LOG.
- KAN-12 Completar retrospectiva y defensa.

## Programación en pareja

Durante cada incremento se alternarán los siguientes roles:

- Driver: escribe código, pruebas y ejecuta los cambios.
- Navigator: revisa decisiones, criterios, casos límite y diseño.

Se aplicará Ping-Pong TDD:

1. Una integrante escribe una prueba que falla.
2. La otra implementa lo mínimo para hacerla pasar.
3. Se intercambian los roles.
4. Se refactoriza con las pruebas en verde.