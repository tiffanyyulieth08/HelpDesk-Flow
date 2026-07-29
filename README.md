# HelpDesk Flow

Sistema de gestión de incidencias desarrollado por Tiffany y Seidy.

## Tecnologías

- Java 17
- Maven
- JUnit 5
- H2
- GitHub Actions

## Persistencia H2

La aplicación utiliza H2 en modo archivo para conservar las incidencias.

Base local:

`./data/helpdesk-flow`

Los archivos generados por H2 no se incluyen en Git.

Las pruebas utilizan bases temporales independientes y no modifican la base local.

Para verificar:

`mvn clean verify`

## Política EXPEDITE

Una incidencia con prioridad CRITICA puede marcarse con clase de servicio
EXPEDITE.

Solo puede existir una incidencia EXPEDITE simultáneamente en los estados:

- EN_DESARROLLO
- EN_VALIDACION

Pueden existir varias incidencias EXPEDITE en REGISTRADA o LISTA. Cuando la
incidencia activa finaliza, otra puede entrar en desarrollo.

## Métricas

HelpDesk Flow calcula las siguientes métricas:

- Total de incidencias.
- Incidencias abiertas.
- Incidencias finalizadas.
- Throughput.
- Lead time promedio de incidencias finalizadas.
- Cantidad de incidencias por prioridad.

Una incidencia se considera abierta cuando su estado es diferente de
`FINALIZADA`.

El lead time se calcula entre la fecha de creación y la fecha de cierre. Las
incidencias abiertas no participan en ese promedio.