# HelpDesk Flow

HelpDesk Flow es una aplicación de consola para registrar, priorizar, consultar y
dar seguimiento a incidencias de soporte. Fue desarrollada por **Tiffany y
Seidy** aplicando Java 17, Maven, JUnit 5, H2, TDD, arquitectura por capas,
Kanban e integración continua con GitHub Actions.

## Requisitos y ejecución

Se requiere JDK 17 y Maven 3.8 o superior. Desde la raíz del proyecto:

```bash
mvn clean package
mvn exec:java
```

El punto de entrada actual es `cr.ac.utn.helpdeskflow.App`. Para ejecutar las
pruebas o la verificación completa:

```bash
mvn test
mvn clean verify
```

## Funcionalidad

- **HU-01 — Registro:** crea una incidencia con UUID y fecha automáticos,
  estado inicial `REGISTRADA` y fecha de cierre nula. Valida título, descripción
  mínima de diez caracteres, categoría, impacto y urgencia.
- **HU-02 — Prioridad automática:** `CalculadoraPrioridad` deriva `NORMAL`,
  `ALTA` o `CRITICA` desde impacto y urgencia. La prioridad no se duplica en la
  base de datos.
- **HU-03 — Flujo:** sólo admite `REGISTRADA → LISTA → EN_DESARROLLO →
  EN_VALIDACION → FINALIZADA`. Finalizar exige una solución y asigna la fecha
  de cierre.
- **HU-04 — Consultas:** permite listar, buscar por UUID, filtrar por estado o
  prioridad y separar abiertas de finalizadas. Es abierta toda incidencia cuyo
  estado sea distinto de `FINALIZADA`.
- **HU-05 — Métricas:** calcula total, abiertas, finalizadas, throughput,
  cantidad por prioridad y lead time promedio. El lead time usa exclusivamente
  incidencias finalizadas y sus fechas de creación y cierre.

## Política EXPEDITE

Sólo una incidencia `CRITICA` puede marcarse `EXPEDITE`. Puede haber varias
EXPEDITE en `REGISTRADA` o `LISTA`, pero sólo una puede ocupar simultáneamente
`EN_DESARROLLO` o `EN_VALIDACION`. Si está en validación, bloquea la entrada de
otra EXPEDITE a desarrollo; al finalizar libera el flujo. Las incidencias
normales no se bloquean.

`IncidenciaWorkflowService` ejecuta la lectura global, validación, transición y
guardado dentro del bloqueo atómico ofrecido por el repositorio. En H2 el
bloqueo se comparte por URL JDBC, incluso entre instancias del repositorio en el
mismo proceso, evitando la secuencia desprotegida de consultar y guardar.

## Persistencia H2

La implementación `H2IncidenciaRepository` conserva UUID, datos descriptivos,
impacto, urgencia, estado, clase de servicio, solución y fechas. El esquema usa
clave primaria y `NOT NULL` para los campos estructuralmente obligatorios; los
enum se guardan por nombre. Las reglas complejas permanecen en el dominio.

La base local se ubica en `./data/helpdesk-flow` al usar una URL como
`jdbc:h2:file:./data/helpdesk-flow`. Sus archivos están ignorados por Git. Las
pruebas emplean directorios temporales y no alteran la base local.

H2 se conserva frente a PostgreSQL porque satisface la persistencia relacional,
integridad y consistencia requeridas para este alcance académico. Migrar al
final y agregar Docker aumentaría el riesgo y la complejidad sin aportar un
criterio funcional faltante.

Para limpiar opcionalmente la base local, cierre la aplicación y elimine los
archivos generados dentro de `data/`. La siguiente ejecución puede crear una
base nueva. Esta acción borra los datos locales.

## Diseño

El proyecto separa dominio, aplicación, contratos de repositorio e
infraestructura. `Incidencia` protege invariantes; `ValidadorTransicion` valida
el flujo; `CalculadoraPrioridad` centraliza la fórmula; los servicios coordinan
consultas, métricas y cambios; H2 e in-memory implementan el mismo puerto de
persistencia. La refactorización preservada se explica en
[`REFACTORIZACION.md`](REFACTORIZACION.md).

```text
src/main/java/cr/ac/utn/helpdeskflow/
├── domain/          entidades, enum y reglas
├── application/     casos de uso, política y métricas
├── repository/      contrato de persistencia
├── infrastructure/  repositorios H2 e in-memory
├── exception/       excepción de negocio
└── App.java         entrada de consola
tests/               pruebas unitarias, funcionales y de persistencia
.github/workflows/   integración continua
```

## Kanban e integración continua

Tablero: [https://github.com/users/tiffanyyulieth08/projects/1]

El workflow `.github/workflows/ci.yml` ejecuta `mvn -B clean verify` en push a
`develop` y `feature/**`, y en pull requests dirigidos a `develop` o `main`.
El estado de CI debe consultarse en GitHub Actions para el commit publicado; la
verificación local final se reproduce con:

```bash
mvn clean verify
```
