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
  | { type: 'date'; message?: string }
  | { type: 'oneOf'; values: string[]; message?: string }
  | { type: 'match'; field: string; label?: string; message?: string }
  | { type: 'regex'; pattern: RegExp; message: string };

export type Schema<T extends Record<string, string>> = Partial<Record<keyof T, ValidationRule[]>>;

export type FormErrors<T extends Record<string, string>> = Partial<Record<keyof T, string>>;

// ─── Engine ───────────────────────────────────────────────────────────────────

export function validateForm<T extends Record<string, string>>(
  schema: Schema<T>,
  values: T
): FormErrors<T> {
  const errors: FormErrors<T> = {};

  for (const field in schema) {
    const rules = schema[field as keyof T];
    if (!rules) continue;

    const raw = values[field as keyof T] ?? '';
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
            error = rule.message ?? 'Solo se permiten numeros.';
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
          const other = (values[rule.field as keyof T] ?? '').toString().trim();
          if (val !== other)
            error = rule.message ?? `Los campos no coinciden.`;
          break;
        }

        case 'regex':
          if (!rule.pattern.test(val)) error = rule.message;
          break;
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

export function clearFieldError<T extends Record<string, string>>(
  errors: FormErrors<T>,
  field: keyof T
): FormErrors<T> {
  if (!errors[field]) return errors;
  const next = { ...errors };
  delete next[field];
  return next;
}
