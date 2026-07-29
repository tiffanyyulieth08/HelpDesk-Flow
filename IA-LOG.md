# Registro de uso de inteligencia artificial

Las entradas describen trabajo verificable en el código, las pruebas y la
documentación del repositorio. No sustituyen la revisión de Tiffany y Seidy.

| Fecha | Herramienta | Objetivo | Resultado usado | Verificación | Cambios humanos |
|---|---|---|---|---|---|
| 2026-07-29 | Codex | Auditar requisitos HU-01 a HU-05 y detectar brechas reales | Matriz de cumplimiento contrastada con dominio, servicios, H2, workflows y 64 pruebas iniciales | Lectura completa del repositorio y `mvn clean verify`: 64 pruebas, 0 fallos, 0 errores | La pareja conserva el diseño existente y limita el alcance a requisitos faltantes |
| 2026-07-29 | Codex | Proteger persistencia y política EXPEDITE | Pruebas para fecha de creación, datos obligatorios e intentos consecutivos de activar dos EXPEDITE; cambio mínimo en repositorio y workflow | Ciclo RED detectó que H2 reemplazaba `fecha_creacion`; GREEN confirma su persistencia y que la segunda EXPEDITE permanece en `LISTA` | El resultado de IA fue modificado por la pareja al mantener `CalculadoraPrioridad`, `ValidadorTransicion` y la estructura actual en vez de aceptar una reescritura amplia |
| 2026-07-29 | Codex | Evaluar PostgreSQL y Docker para la entrega | Sugerencia rechazada: migración inmediata de H2 a PostgreSQL y Docker | Pruebas H2 cubren reinicio del repositorio, recuperación completa y política EXPEDITE; el workflow verifica sin servicios externos | Rechazo técnico: H2 cubre el alcance, la migración no es obligatoria y elevaría el riesgo final sin mejorar directamente los criterios funcionales |
| 2026-07-29 | Codex | Completar evidencia académica | README ampliado, registro de IA y retrospectiva específica del repositorio | Revisión de enlaces, comandos, archivos requeridos y verificación Maven final | Tiffany y Seidy deben agregar el enlace real del tablero y validar que la narración refleje su experiencia antes de la defensa |

La IA puede omitir contexto humano o proponer más complejidad de la necesaria.
Por eso cada resultado se acepta sólo cuando existe evidencia en código,
documentos o pruebas automatizadas.
