# Spec: Generación de Documentación del Sistema (HIS-Project)

## Contexto del Proyecto
El proyecto ha sufrido modificaciones durante su desarrollo. Por lo tanto, tu principal directriz es **NO asumir funcionalidades**. Debes basar toda la documentación estrictamente en el estado actual del código en la rama `develop` y en los archivos `.md` de los Casos de Uso ya existentes en el repositorio.

La base de datos es administrada externamente, por lo que su documentación será a nivel conceptual (conexiones y esquemas referenciados en el código), sin generar scripts de creación.

## Tarea 1: Generar el Manual de Usuario
Basándote en la plantilla oficial, genera un archivo `docs/Manual_de_Usuario.md`. Debes extraer la funcionalidad de los casos de uso `.md` ya subidos.
La estructura debe seguir este orden:
1. [cite_start]**Descripción del Sistema:** Incluye Objeto [cite: 25][cite_start], Alcance [cite: 26] [cite_start]y Funcionalidad[cite: 27].
2. [cite_start]**Mapa del Sistema:** Modelo Lógico [cite: 29] [cite_start]y Navegación[cite: 30].
3. [cite_start]**Descripción de Subsistemas:** Detalla las pantallas principales y los mensajes de error asociados [cite: 32, 33, 34] basándote en los flujos de excepción de los casos de uso.
4. [cite_start]**FAQ:** Preguntas frecuentes derivadas de los flujos de la aplicación[cite: 35].

## Tarea 2: Generar el Manual Técnico
Genera un archivo `docs/Manual_Tecnico.md` consolidando la arquitectura del proyecto. Debe incluir:
1. **Herramientas Utilizadas:** Escanea los archivos `package.json`, `pom.xml`, y `docker-compose.yml` para listar frameworks, librerías y tecnologías reales utilizadas en frontend y backend.
2. **Versionado en GitHub:** Documenta la estrategia de ramas (ej. uso de `develop`, `main`) y convenciones de commits observadas en el historial.
3. **Integración de Diagramas:** Crea una sección de Arquitectura que referencie explícitamente los diagramas que ya están en el repositorio:
   - Diagrama de Base de Datos (ER).
   - Diagrama de Secuencia.
   - Diagrama de Clases.
   - Estructura de Módulos.
   - Diagrama de Despliegue.
4. **Base de Datos:** Explica la estructura de esquemas lógicos (Schema-per-service) a los que se conecta la aplicación, mencionando que la administración es externa.

## Tarea 3: Plan de Pruebas de Software (Opcional)
Genera una plantilla de pruebas en `docs/Plan_de_Pruebas.md` para las funcionalidades principales. Por cada caso de uso clave, define:
- [cite_start]Datos de entrada, reglas de negocio, flujos básicos, flujos alternos y flujos de excepción[cite: 7].
- [cite_start]Precondiciones y pasos secuenciales para ejecutar la prueba[cite: 10].

## Reglas de Ejecución para Kiro
- **Fase 1:** Lee todos los archivos `.md` de la carpeta de casos de uso y escanea los archivos de dependencias (`package.json`, etc.).
- **Fase 2:** Redacta los documentos en formato Markdown dentro de una nueva carpeta `docs/` en la raíz del proyecto.
- **Fase 3:** Solicita mi revisión (del usuario) antes de dar por completado el Spec.