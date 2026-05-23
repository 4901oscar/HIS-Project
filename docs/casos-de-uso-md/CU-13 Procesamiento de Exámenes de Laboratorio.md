Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-13 — Procesamiento de Exámenes de Laboratorio
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al personal de triaje registrar los signos vitales del paciente y aplicar la clasificación Manchester para determinar su nivel de urgencia, derivándolo a consulta médica.
Definición Caso de Uso
Actores.
Actor principal: Técnico de Laboratorio
Sistema.

Precondiciones.
Existe una cita con estado LAB_SAMPLE_COLLECTION (generada por CU-12 al seleccionar destino Laboratorio)
El técnico tiene sesión activa con rol LABORATORY
Flujo Normal Básico
El técnico ingresa a Gestión de Laboratorio (/lab) y ve la lista de citas con estado de Recolección de Muestras
El técnico presiona Atender en la fila del paciente; el sistema reproduce una notificación de audio y navega a la vista del Flujo de Laboratorio.
El sistema muestra el Paso 1 — Recolección de Muestras con la lista de exámenes solicitados, indicando nombre del examen, tipo de examen y tipo de muestra requerida
El técnico recolecta físicamente las muestras y presiona Recolectar Muestras; el sistema actualiza el estado de la cita a Recolección de Muestras y avanza al Paso 2
El sistema muestra el Paso 2 — Validar Muestras con la lista de muestras recibidas
El técnico inspecciona la calidad de las muestras y presiona Aceptar muestras; el sistema actualiza el estado a En Procesamiento y avanza al Paso 3 [FA01]
El sistema muestra el Paso 3 — Procesar Exámenes con un componente de carga de archivo por cada examen solicitado [FA02][FA03]
Por cada examen, el técnico selecciona el archivo de resultado (PDF, JPEG o PNG, máx. 10 MB) y el sistema lo sube al servidor, confirmando la carga con una marca visual en ese examen
Una vez que todos los exámenes tienen resultado cargado, el técnico presiona Marcar como completado; el sistema valida con el backend que todos los exámenes tienen resultado, actualiza el estado a Resultados Listos y avanza al Paso 4
El sistema muestra el Paso 4 — Resultados Listos con la lista de archivos cargados (nombre del examen, nombre de archivo, fecha de carga) y opción de descargar cada resultado
El técnico revisa que todo esté correcto y presiona Enviar al doctor; el sistema actualiza el estado de la cita a Consulta y navega de regreso a la lista de laboratorio
La cita aparece en la cola del doctor con el estado CONSULTATION y los resultados disponibles en la sección de resultados de laboratorio
Flujos Alternos.
FA01 Muestras rechazadas (Paso 2)
En el Paso 2, el técnico detecta que las muestras son insuficientes, están contaminadas o mal etiquetadas
El técnico presiona Solicitar nueva muestra
El sistema actualiza el estado de la cita de vuelta a Recolección de Muestras
El wizard regresa al Paso 1 para que el paciente sea llamado nuevamente a toma de muestras
El flujo continúa desde el paso 4 del flujo normal
FA02 — Formato de archivo inválido (Paso 3)
El técnico intenta subir un archivo con formato no permitido (distinto a PDF, JPEG, PNG) o mayor a 10 MB
El sistema rechaza la carga y muestra un mensaje de error sobre ese examen específico
Los demás exámenes no se ven afectados
El técnico selecciona un archivo válido y continúa
FA03 — Exámenes incompletos al marcar como completado (Paso 3)
El técnico presiona Marcar como completado sin haber subido resultado para todos los exámenes
El sistema consulta al backend si todos los exámenes tienen resultado; el backend responde negativo
El sistema muestra un mensaje de error indicando que faltan resultados por cargar
El técnico permanece en el Paso 3 y completa las cargas pendientes.

Postcondiciones.
Los archivos de resultado quedan almacenados en el lab-service asociados a cada examen
La cita transiciona al estado Consulta
El doctor puede ver los resultados desde su formulario de consulta (LabResultsSection)
Requerimientos Suplementarios o no Funcionales
Los resultados cargados en el Paso 3 persisten aunque el técnico recargue la página (se recuperan del backend al montar el componente)
El tamaño máximo de archivo por resultado es 10 MB; los formatos aceptados son PDF, JPEG y PNG
La lista de laboratorio no tiene auto-refresco; el técnico debe presionar Actualizar manualmente
Firmas Necesarias
En este punto se debe incluir la firma de aquellas personas que deban aprobar la presente especificación.


| Nombre | Fecha | Descripción del Cambio | Versión |
|---|---|---|---|
| Oscar Rivera | 19/02/2026 | Definición inicial. | 1.0 |
| Jose Avila | 20/02/2026 | Definición inicial. | 1.1 |
| Equipo HIS | 08/04/2026 | Modificación de flujo básico, flujos alternos y se hace referencia a RN. | 1.2 |
|  |  |  |  |
|  |  |  |  |


| Persona | Sector | Firma |
|---|---|---|
|  |  |  |
|  |  |  |
