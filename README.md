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

```bash
mvn clean verify