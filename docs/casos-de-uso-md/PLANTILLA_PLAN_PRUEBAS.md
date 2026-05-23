**Plan de pruebas de software**

***[Nombre del proyecto]***

***Fecha: [dd/mm/aaa]***

# Historial de versiones

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Fecha** | **Versión** | **Autor** | **Organización** | **Descripción** |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |

# Información del proyecto

|  |  |
| --- | --- |
| Empresa / Organización |  |
| Proyecto |  |
| Fecha de preparación |  |
| Cliente |  |
| Patrocinador principal |  |
| Gerente / Líder de proyecto |  |
| Gerente / Líder de pruebas de software |  |

# Aprobaciones

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Nombre y Apellido** | **Cargo** | **Departamento u organización** | **Fecha** | **Firma** |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |

# Resumen ejecutivo

Resumen de todo el contenido del plan de pruebas de software, describe cuál es su propósito, establece si es un plan maestro o un plan detallado, identifica el alcance del plan de pruebas en relación con el plan de proyecto de software, restricciones (por ejemplo de recursos o presupuesto), alcance del esfuerzo de pruebas entre otros aspectos.

# Alcance de las pruebas

## Elementos de pruebas

Listado de todos los módulos, componentes o elementos que se van a probar. Si es de alto nivel, se listan las áreas funcionales (módulos o procesos que cubre el Testing), por otro lado, si es de un nivel detallado se listan los programas, unidades o módulos.

## Nuevas funcionalidades a probar

Es un listado de lo que se va a probar “desde el punto de vista del usuario”. No es una descripción técnica del software sino sus características y funcionalidades. Se incluyen tanto las que son nuevas como las que se están modificando.

Cada conjunto de casos de prueba para cada caso de uso deberá contemplar:

|  |  |
| --- | --- |
| **ELEMENTO DEL CASO DE USO** | **CASO DE PRUEBA** |
| **Datos de entrada** | Verificar que los datos de entrada cumplan con:   * Obligatoriedad * Tipo de datos * Longitud * Estructura |
| **Reglas de Negocio** | Validar reglas de negocio que afecten los datos de entrada (Dependencia de datos). |
| Validar reglas de negocio que afecten los flujos. |
| **Flujos Alternos** | Verificar la ejecución de todos los flujos alternos. |
| **Flujos de Excepción** | Verificar la ejecución de todos los flujos de Excepción. |
| **Flujo Básico** | Verificar la ejecución del flujo básico. |
| **Generalidades:** | Los casos de prueba deben especificar exactamente rutas, nombres de archivos, valores para los datos de entrada.  Para asegurar que las rutas y nombres de archivos se cumplan; deberá instalarse una árbol de carpetas predefinido en la estación donde se ejecutará la prueba. |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA** | | | | | |
| **CASO DE PRUEBA No.** | *<Número del caso de prueba constituido [número del caso de uso]-[Numero del caso de prueba]>* | | **VERSIÓN DE EJECUCIÓN** | | *<Versión diligenciado por el analista de pruebas en el momento de ejecutarla. Este número se incrementa de 1 en 1>* |
| **FECHA EJECUCIÓN** | | *<Fecha de ejecución diligenciado por el analista de pruebas>* |
| **CASO DE USO:** | *<Identificación del caso de uso objeto de la prueba>* | | **MODULO DEL SISTEMA** | | *<Nombre del modulo al que corresponde el caso de uso objeto de la prueba>* |
| **Descripción del caso de prueba:** | *<Descripción de lo que se pretende probar en el caso de prueba>* | | | | |
| 1. **CASO DE PRUEBA** | | | | | |
| 1. **Precondiciones** | | | | | |
| *<Lista de precondiciones que deben cumplirse para realizar la prueba>* | | | | | |
| 1. **Pasos de la prueba** | | | | | |
| *<Pasos secuenciales que deben ser ejecutados por el analista de pruebas o usuario, ante el sistema para ejecutar la prueba debe incluir pantallas del funcionamiento>* | | | | | |
| 1. **RESULTADOS DE LA PRUEBA** | | | | | |
| **Hallazgos** | | | | **Veredicto** | |
| *<Lista de defectos o desviaciones encontrados por el analista o usuario al ejecutar la prueba>* | | | | **Aprobación**  **Hallazgo** | |
| **Observaciones** | | **Analista de QA** | | | |
| *<Observaciones generales del analista o usuario sobre la ejecución de la prueba>* | |  | | | |
| **Firma:**  **Nombre:**  **Fecha:** | | | |
