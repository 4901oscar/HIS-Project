Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-15: Cobros y Facturación
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al cajero procesar los cobros pendientes de consulta, laboratorio y farmacia, registrando el pago y la forma de pago del paciente, y habilitando la continuación del flujo de atención hacia el módulo correspondiente.
Definición Caso de Uso
Actores.
Actor principal: Cajero
Sistema.

Precondiciones.
El cajero tiene sesión activa con rol Cajero.
Existe al menos una cita en estado Pendiente de Pago de Consulta, Pendiente de Pago de Laboratorio o Pendiente de Pago de Farmacia con factura generada.
La factura asociada a la cita tiene estado Pendiente.
Flujo Normal Básico
El cajero ingresa al Módulo de Caja; el sistema carga y muestra tres secciones independientes: Cobros de Consulta, Cobros de Laboratorio y Cobros de Farmacia, cada una con su lista de citas pendientes de pago ordenadas por fecha y hora.
El cajero localiza al paciente en la sección correspondiente e identifica el número de factura, el monto total y el estado del pago.
El cajero presiona Cobrar en la fila del paciente; el sistema carga el detalle de la factura y abre el modal de pago mostrando el desglose de cargos, subtotal y total a pagar.
El cajero selecciona la forma de pago: Efectivo, Tarjeta o Transferencia. [FA01]
El cajero ingresa el NIT del cliente (por defecto "CF" para consumidor final) y el nombre del cliente.
El cajero presiona Confirmar Pago; el sistema registra el pago, cambia el estado de la factura a Pagada y muestra el resumen del pago con el cambio a devolver si aplica.
Para cobros de Laboratorio: el sistema transiciona la cita de Pendiente de Pago de Laboratorio a En Laboratorio y la cita aparece en la cola del técnico de laboratorio.
Para cobros de Farmacia: el sistema transiciona la cita de Pendiente de Pago de Farmacia a En Farmacia y la cita aparece en la cola del farmacéutico.
El cajero presiona Continuar; el sistema cierra el modal y recarga las tres listas, desapareciendo la cita cobrada de la cola correspondiente.
Fin del caso de uso.
Flujos Alternos.
FA01 — Pago en efectivo con cambio
1.	En el paso 2.3.4, el cajero selecciona Efectivo como forma de pago.
2.	El sistema habilita el campo Monto Recibido.
3.	El cajero ingresa el monto entregado por el paciente.
4.	El sistema calcula y muestra en tiempo real el cambio a devolver.
5.	Si el monto ingresado es menor al total de la factura, el sistema deshabilita el botón Confirmar Pago y muestra el mensaje: "El monto recibido debe ser al menos Q XXX.XX". [FA02]
6.	El cajero ajusta el monto y el flujo continúa desde el paso 2.3.6.
FA02 — Monto insuficiente
1.	El cajero ingresa un monto en efectivo menor al total de la factura.
2.	El sistema deshabilita el botón Confirmar Pago y muestra el error correspondiente.
3.	El cajero solicita al paciente el monto faltante o cambia la forma de pago.
4.	El flujo retoma desde el paso 2.3.4.
FA03 — Paciente no tiene NIT
1.	En el paso 2.3.5, el paciente indica que no posee NIT.
2.	El cajero deja el campo NIT con el valor por defecto "CF" (Consumidor Final).
3.	El flujo continúa normalmente desde el paso 2.3.6.

Postcondiciones.
La factura queda registrada con estado Pagada en el sistema, incluyendo el NIT, nombre del cliente, forma de pago y monto recibido.
Para cobros de consulta: la cita continúa su flujo normal según lo determinado por el médico (hacia laboratorio, farmacia o completada).
Para cobros de laboratorio: la cita transiciona a En Laboratorio y queda disponible en la cola del técnico de laboratorio (CU-13).
Para cobros de farmacia: la cita transiciona a En Farmacia y queda disponible en la cola del farmacéutico (CU-14).
El cambio entregado al paciente queda registrado en el comprobante de pago.
Requerimientos Suplementarios o no Funcionales
Las tres listas de cobros no tienen auto-refresco; el cajero debe presionar Actualizar manualmente en cada sección para ver nuevas citas.
El sistema acepta las formas de pago: Efectivo, Tarjeta de crédito/débito y Transferencia bancaria.
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
