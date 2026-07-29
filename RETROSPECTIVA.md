# Retrospectiva de HelpDesk Flow

Kanban aportó una representación clara del trabajo desde las opciones hasta
Hecho. Las políticas descritas en `KANBAN.md` ayudaron a que una tarjeta entrara
a desarrollo con criterios verificables, pruebas identificadas y roles de
Driver y Navigator. También hizo visible que persistencia, EXPEDITE, métricas,
integración continua y documentación no eran tareas aisladas, sino incrementos
del mismo flujo de entrega.

La principal dificultad del límite WIP de uno en En desarrollo y Validación es
que obliga a terminar o desbloquear una tarea antes de iniciar otra. Esto reduce
trabajo parcialmente terminado, pero exige coordinación: si una prueba,
revisión o decisión técnica queda pendiente, no se puede compensar abriendo
varias tarjetas. En este proyecto esa restricción resulta útil porque favorece
incrementos pequeños y verificables, aunque puede sentirse lenta durante la
espera de una revisión.

TDD permitió detectar errores y proteger reglas concretas. Las pruebas cubren
las nueve combinaciones de impacto y urgencia, saltos y retrocesos de estado,
cierre sin solución, consultas, métricas y persistencia. En la auditoría final,
una nueva aserción de persistencia produjo un fallo real: H2 generaba
`fecha_creacion` mediante el valor predeterminado de la tabla porque el
`INSERT` no enviaba la fecha creada por el dominio. El cambio mínimo fue
persistir y mapear esa fecha. Otra prueba protege intentos consecutivos de
activar dos EXPEDITE persistidas y verifica que la segunda permanezca en
`LISTA`.

La refactorización existente extrajo la validación secuencial a
`ValidadorTransicion` y encapsuló el acceso a la prioridad mediante
`Incidencia.getPrioridad()`, manteniendo la fórmula en
`CalculadoraPrioridad`. Esto redujo responsabilidades mezcladas sin cambiar el
comportamiento observable. Se conservaron esas decisiones porque ya cuentan
con pruebas protectoras y cumplen la rúbrica; una reescritura cosmética al
cierre habría añadido riesgo.

El cambio EXPEDITE agregó una excepción controlada al flujo normal. Varias
incidencias críticas pueden esperar en `REGISTRADA` o `LISTA`, pero sólo una
EXPEDITE puede ocupar el tramo activo formado por `EN_DESARROLLO` y
`EN_VALIDACION`. Esto evita competir por la capacidad urgente, permite liberar
el espacio al finalizar y no impide que una incidencia normal avance. La
auditoría agrupó consulta, validación, transición y guardado bajo el bloqueo
atómico del repositorio; H2 comparte dicho bloqueo por URL dentro del proceso.

La IA ayudó a comparar requisitos contra evidencia existente, señalar la fecha
de creación que no se recuperaba correctamente, proponer pruebas de regresión y
organizar la documentación. Su limitación principal es que puede sugerir
tecnologías o refactorizaciones mayores sin valorar suficientemente el momento
de la entrega. Por esa razón se rechazó migrar inmediatamente a PostgreSQL y
Docker: H2 ya satisface la persistencia requerida y la migración aumentaría
complejidad sin mejorar los criterios funcionales.

En una siguiente versión convendría ampliar la interfaz de consola para operar
todos los casos de uso, incorporar una abstracción transaccional explícita si
la aplicación pasa a múltiples procesos, publicar el enlace definitivo del
tablero y añadir reportes de cobertura. Estas mejoras deben entrar como
incrementos separados, con criterios de aceptación y pruebas, sin debilitar la
simplicidad alcanzada.
