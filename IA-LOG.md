# Registro de uso de inteligencia artificial

Las entradas describen trabajo verificable en el código, las pruebas y la
documentación del repositorio. No sustituyen la revisión de Tiffany y Seidy.

| Fecha | Herramienta | Objetivo | Resultado usado | Verificación | Cambios humanos |
|---|---|---|---|---|---|
| 2026-07-29 | Codex | Auditar requisitos HU-01 a HU-05 y detectar brechas reales | Matriz de cumplimiento contrastada con dominio, servicios, H2, workflows y 64 pruebas iniciales | Lectura completa del repositorio y `mvn clean verify`: 86 pruebas, 0 fallos, 0 errores | La pareja conserva el diseño existente y limita el alcance a requisitos faltantes |
| 2026-07-29 | Codex | Proteger persistencia y política EXPEDITE | Pruebas para fecha de creación, datos obligatorios e intentos consecutivos de activar dos EXPEDITE; cambio mínimo en repositorio y workflow | Ciclo RED detectó que H2 reemplazaba `fecha_creacion`; GREEN confirma su persistencia y que la segunda EXPEDITE permanece en `LISTA` | El resultado de IA fue modificado por la pareja al mantener `CalculadoraPrioridad`, `ValidadorTransicion` y la estructura actual en vez de aceptar una reescritura amplia |
| 2026-07-29 | Codex | Evaluar PostgreSQL y Docker para la entrega | Sugerencia rechazada: migración inmediata de H2 a PostgreSQL y Docker | Pruebas H2 cubren reinicio del repositorio, recuperación completa y política EXPEDITE; el workflow verifica sin servicios externos | Rechazo técnico: H2 cubre el alcance, la migración no es obligatoria y elevaría el riesgo final sin mejorar directamente los criterios funcionales |
| 2026-07-29 | Codex | Completar evidencia académica | README ampliado, registro de IA y retrospectiva específica del repositorio | Revisión de enlaces, comandos, archivos requeridos y verificación Maven final | Tiffany y Seidy deben agregar el enlace real del tablero y validar que la narración refleje su experiencia antes de la defensa |

La IA puede omitir contexto humano o proponer más complejidad de la necesaria.
Por eso cada resultado se acepta sólo cuando existe evidencia en código,
documentos o pruebas automatizadas.

## Interacción — Cálculo de prioridad

- Herramienta: Codex
- Responsable: Seidy

### Objetivo

Solicitar apoyo para expresar mediante pruebas las reglas de prioridad de las
incidencias.

### Resultado obtenido

La herramienta propuso casos para prioridad NORMAL, ALTA y CRITICA de acuerdo
con impacto y urgencia.

### Verificación

La propuesta se verificó mediante:

- `CalculadoraPrioridadTest`
- `mvn test`
- `mvn clean verify`

### Cambios humanos

Se adaptaron los nombres, paquetes y casos de prueba a la estructura real del
proyecto. También se separaron los commits RED, GREEN y REFACTOR.

## Interacción — Política EXPEDITE

- Herramienta: Codex
- Responsable: Seidy

### Objetivo

Definir pruebas y diseño para permitir únicamente una incidencia EXPEDITE activa.

### Resultado obtenido

La herramienta propuso una validación individual y una política global para
controlar las incidencias en EN_DESARROLLO o EN_VALIDACION.

### Verificación

Se verificó mediante:

- `ExpediteTest`
- `ExpediteWorkflowTest`
- `H2ExpeditePersistenceTest`
- `mvn clean verify`

### Cambios humanos

La pareja reforzó las pruebas con assertions explícitas para comprobar
ClaseServicio.EXPEDITE, estado REGISTRADA, rechazo de la segunda incidencia y
liberación del límite al finalizar la primera.

## Interacción — Integración continua

- Herramienta: Codex
- Responsable: Seidy

### Objetivo

Configurar y comprobar la ejecución automática de compilación y pruebas en
GitHub Actions.

### Resultado obtenido

Se obtuvo una propuesta de workflow Maven con Java 17 para ejecutarse en push y
Pull Request.

### Verificación

Se verificó observando:

- ejecución roja durante RED;
- ejecución verde durante GREEN;
- ejecución verde durante REFACTOR;
- `BUILD SUCCESS`;
- cero fallos y cero errores.

### Cambios humanos

Se adaptaron la rama objetivo, la versión de Java y los comandos Maven al
proyecto real.

## Sugerencia rechazada

Durante el desarrollo de EXPEDITE, la IA sugirió colocar toda la restricción
dentro de la entidad `Incidencia`.

La pareja rechazó esa sugerencia porque una incidencia individual no conoce las
demás incidencias almacenadas. Para determinar si ya existe otra EXPEDITE activa
es necesario consultar el repositorio.

La decisión humana fue mantener:

- la validación individual en la entidad;
- la validación global en `ExpeditePolicy`;
- la coordinación en `IncidenciaWorkflowService`.

La decisión fue verificada mediante `ExpediteWorkflowTest` y
`mvn clean verify`.

## Interacción — Registro de incidencias


- Herramienta: Codex
- Responsable: Tiffany

### Objetivo

Solicitar apoyo para crear las pruebas de registro de incidencias siguiendo TDD.

### Resultado obtenido

La herramienta propuso pruebas para UUID, título, descripción, categoría,
impacto y urgencia.

### Verificación

Se verificó mediante:

- `IncidenciaTest`
- ejecución RED antes de implementar;
- ejecución GREEN después de implementar;
- `mvn clean verify`.

### Cambios humanos

La pareja adaptó los constructores, nombres y validaciones a las convenciones
reales del proyecto y mantuvo separados los commits RED, GREEN y REFACTOR.

## Interacción — Consultas y filtros


- Herramienta: Codex
- Responsable: Tiffany

### Objetivo

Diseñar las pruebas y la implementación para listar, buscar y filtrar
incidencias.

### Resultado obtenido

Se propuso un servicio de consultas y un repositorio en memoria con filtros por
estado, prioridad, abiertas y finalizadas.

### Verificación

Se verificó mediante:

- `IncidenciaConsultaServiceTest`
- `ConsultaIncidenciasFuncionalTest`
- `mvn clean verify`.

### Cambios humanos

Se modificó la propuesta para que el filtro consultara
`incidencia.getPrioridad()` y no volviera a conocer directamente cómo se calcula
la prioridad.

## Interacción — Persistencia H2


- Herramienta: Codex
- Responsable: Tiffany

### Objetivo

Diseñar pruebas de integración y una implementación JDBC para conservar
incidencias en H2.

### Resultado obtenido

La herramienta propuso `H2IncidenciaRepository`, creación automática del
esquema, consultas preparadas y una base temporal para pruebas.

### Verificación

Se verificó mediante:

- `H2IncidenciaRepositoryTest`
- `H2ExpeditePersistenceTest`
- prueba de recreación del repositorio;
- `mvn clean verify`.

### Cambios humanos

La pareja adaptó el esquema a los campos reales, agregó soporte para
`ClaseServicio`, utilizó `@TempDir` en pruebas y excluyó los archivos locales H2
mediante `.gitignore`.

