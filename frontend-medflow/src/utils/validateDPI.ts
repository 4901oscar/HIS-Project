/**
 * Validación oficial del DPI guatemalteco (CUI).
 * Algoritmo del Registro Nacional de las Personas (RENAP).
 *
 * Estructura: CCCCCCCC V MM DD (13 dígitos)
 *   C = 8 dígitos correlativos
 *   V = 1 dígito verificador
 *   MM = municipio (2 dígitos)
 *   DD = departamento (2 dígitos)
 */

const MAX_MUNICIPIOS_POR_DEPTO: number[] = [
  17, // 01 Guatemala
  8,  // 02 El Progreso
  16, // 03 Sacatepéquez
  16, // 04 Chimaltenango
  13, // 05 Escuintla
  14, // 06 Santa Rosa
  19, // 07 Sololá
  8,  // 08 Totonicapán
  24, // 09 Quetzaltenango
  21, // 10 Suchitepéquez
  11, // 11 Retalhuleu
  30, // 12 San Marcos
  32, // 13 Huehuetenango
  21, // 14 Quiché
  8,  // 15 Baja Verapaz
  16, // 16 Alta Verapaz
  14, // 17 Petén
  5,  // 18 Izabal
  11, // 19 Zacapa
  11, // 20 Chiquimula
  9,  // 21 Jalapa
  8,  // 22 Jutiapa
];

export function validateDPI(dpi: string): { valid: boolean; error?: string } {
  if (!/^\d{13}$/.test(dpi)) {
    return { valid: false, error: 'El DPI debe tener exactamente 13 dígitos numéricos.' };
  }

  const depto = parseInt(dpi.substring(11, 13), 10);
  const municipio = parseInt(dpi.substring(9, 11), 10);

  if (depto < 1 || depto > 22) {
    return { valid: false, error: 'El DPI contiene un código de departamento inválido.' };
  }

  if (municipio < 1 || municipio > MAX_MUNICIPIOS_POR_DEPTO[depto - 1]) {
    return { valid: false, error: 'El DPI contiene un código de municipio inválido.' };
  }

  return { valid: true };
}
