Introducción
Descripción:
Describe el proceso mediante el cual el cajero atiende los cobros de los pacientes por los distintos servicios de la clínica. El sistema genera la factura electrónica automáticamente al confirmar cada pago.

Objetivo:
Centralizar todos los cobros en un solo módulo, garantizando que ningún servicio sea prestado sin pago previo y cumpliendo con la emisión de factura electrónica.

Actores
•	Cajero.
•	Sistema.

Condiciones de Inicio
•	El cajero debe haber ingresado al sistema (ver Caso de Uso 00.1).
•	El paciente debe tener un cobro pendiente en su expediente.

Flujo Normal
1.	El cajero ingresa al módulo de facturación. El sistema muestra la lista de pacientes con cobros pendientes.
2.	El cajero llama al paciente y lo selecciona en el sistema.
3.	El sistema muestra el detalle del cobro: servicios, subtotales y total.  [RN19]
4.	El cajero solicita al paciente su NIT y nombre para la factura.  [RN02]
5.	El paciente indica su forma de pago: efectivo o tarjeta.
6.	El cajero procesa el pago y confirma la transacción.  [FA01]
7.	El sistema genera automáticamente la factura electrónica.  [FA02]
8.	El sistema entrega la factura al paciente de forma impresa o por correo electrónico.
9.	El sistema actualiza el estado del paciente para que continúe con el siguiente paso de su atención.
10.	Fin del caso de uso.

Flujos Alternos
FA01 — Pago con tarjeta rechazado
11.	El cobro con tarjeta no es aprobado.
12.	El cajero informa al paciente. El paciente puede ofrecer otro método de pago.
13.	Si no puede pagar, el cajero cancela. El sistema mantiene el cobro pendiente y pausa la atención.

FA02 — Falla en la emisión de factura electrónica
14.	El sistema no puede conectarse al servicio de facturación electrónica.
15.	El sistema genera un comprobante de pago temporal para no detener la atención del paciente.
16.	El sistema emite la factura electrónica automáticamente cuando el servicio esté disponible.
17.	El flujo continúa desde el paso 9.
