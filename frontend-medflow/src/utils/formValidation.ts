import { validateDPI } from './validateDPI';

// ─── Rule definitions ─────────────────────────────────────────────────────────

export type ValidationRule =
  | { type: 'required'; message?: string }
  | { type: 'minLength'; min: number; message?: string }
  | { type: 'maxLength'; max: number; message?: string }
  | { type: 'dpi'; message?: string }
  | { type: 'email'; message?: string }
  | { type: 'phone'; message?: string }
  | { type: 'digits'; message?: string }
  | { type: 'alphaName'; message?: string }
  | { type: 'date'; message?: string }
  | { type: 'oneOf'; values: string[]; message?: string }
  | { type: 'match'; field: string; label?: string; message?: string }
  | { type: 'regex'; pattern: RegExp; message: string }
  | { type: 'range'; min: number; max: number; message?: string };

export type Schema<T> = { [K in keyof T]?: ValidationRule[] };

export type FormErrors<T> = { [K in keyof T]?: string };

// ─── Engine ───────────────────────────────────────────────────────────────────

export function validateForm<T>(
  schema: Schema<T>,
  values: T
): FormErrors<T> {
  const errors: FormErrors<T> = {};
  const map = values as Record<string, unknown>;

  for (const field in schema) {
    const rules = schema[field as keyof T];
    if (!rules) continue;

    const raw = map[field] ?? '';
    const val = typeof raw === 'string' ? raw.trim() : String(raw);

    for (const rule of rules) {
      let error: string | null = null;

      switch (rule.type) {
        case 'required':
          if (!val) error = rule.message ?? 'Este campo es requerido.';
          break;

        case 'minLength':
          if (val.length < rule.min)
            error = rule.message ?? `Debe tener al menos ${rule.min} caracteres.`;
          break;

        case 'maxLength':
          if (val.length > rule.max)
            error = rule.message ?? `No puede superar ${rule.max} caracteres.`;
          break;

        case 'dpi': {
          const result = validateDPI(val);
          if (!result.valid) error = result.error ?? 'DPI invalido.';
          break;
        }

        case 'email':
          if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val))
            error = rule.message ?? 'Ingresa un correo electronico valido.';
          break;

        case 'phone':
          if (!/^\d{8}$/.test(val))
            error = rule.message ?? 'El telefono debe tener exactamente 8 digitos.';
          break;

        case 'digits':
          if (!/^\d+$/.test(val))
            error = rule.message ?? 'Dato inválido.';
          break;

        case 'alphaName':
          if (!/^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ0-9\s\-]+$/.test(val))
            error = rule.message ?? 'Dato inválido.';
          break;

        case 'date':
          if (!val || !/^\d{4}-\d{2}-\d{2}$/.test(val))
            error = rule.message ?? 'Formato de fecha invalido (YYYY-MM-DD).';
          break;

        case 'oneOf':
          if (!rule.values.includes(val))
            error = rule.message ?? `Valor invalido. Opciones: ${rule.values.join(', ')}.`;
          break;

        case 'match': {
          const other = (map[rule.field] ?? '').toString().trim();
          if (val !== other)
            error = rule.message ?? `Los campos no coinciden.`;
          break;
        }

        case 'regex':
          if (!rule.pattern.test(val)) error = rule.message;
          break;

        case 'range': {
          if (!val) break;
          const num = parseFloat(val);
          if (isNaN(num) || num < rule.min || num > rule.max)
            error = rule.message ?? `El valor debe estar entre ${rule.min} y ${rule.max}.`;
          break;
        }
      }

      if (error) {
        errors[field as keyof T] = error;
        break;
      }
    }
  }

  return errors;
}

// ─── Helper — limpia errores al cambiar un campo ──────────────────────────────

export function clearFieldError<T>(
  errors: FormErrors<T>,
  field: keyof T
): FormErrors<T> {
  if (!errors[field]) return errors;
  const next = { ...errors };
  delete next[field];
  return next;
}

// ─── Helper — bloqueo en tiempo real (onChange) ───────────────────────────────
// Retorna el mensaje de error si el valor debe ser bloqueado, null si es válido.

export function getBlockingError(rules: ValidationRule[], value: string): string | null {
  for (const rule of rules) {
    if (rule.type === 'digits' && /[^\d]/.test(value))
      return rule.message ?? 'Dato inválido.';
    if (rule.type === 'alphaName' && /[^a-zA-ZáéíóúÁÉÍÓÚüÜñÑ0-9\s\-]/.test(value))
      return rule.message ?? 'Dato inválido.';
  }
  return null;
}

// ─── Helper — validación al salir del campo (onBlur) ─────────────────────────
// Valida solo reglas de formato (email, phone, minLength). Ignora required.

export function validateFieldOnBlur<T>(
  schema: Schema<T>,
  values: T,
  field: keyof T
): string | undefined {
  const rules = schema[field];
  if (!rules) return undefined;
  const map = values as Record<string, unknown>;
  const val = (map[field as string] ?? '').toString().trim();
  if (!val) return undefined;

  for (const rule of rules) {
    if (rule.type === 'required') continue;
    let error: string | null = null;

    switch (rule.type) {
      case 'email':
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val))
          error = rule.message ?? 'Ingresa un correo electrónico válido.';
        break;
      case 'phone':
        if (!/^\d{8}$/.test(val))
          error = rule.message ?? 'El teléfono debe tener exactamente 8 dígitos.';
        break;
      case 'minLength':
        if (val.length < rule.min)
          error = rule.message ?? `Debe tener al menos ${rule.min} caracteres.`;
        break;
      case 'match': {
        const other = (map[rule.field] ?? '').toString().trim();
        if (val !== other) error = rule.message ?? 'Los campos no coinciden.';
        break;
      }
    }
    if (error) return error;
  }
  return undefined;
}
