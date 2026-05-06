# Usuarios de Prueba - Frontend

Los usuarios están definidos en `src/services/mockData.ts` y pueden usarse **únicamente en el frontend** para testing sin necesidad del backend.

## Usuarios Disponibles

| Usuario | Contraseña | Rol | Email |
|---------|-----------|-----|-------|
| `admin` | `Admin1234` | Administrador | admin@medflow.com |
| `doctor` | `Doctor1234` | Médico | doctor@medflow.com |
| `admision` | `Admision1234` | Admisión | admision@medflow.com |
| `pharmacy` | `Pharmacy1234` | Farmacéutico | pharmacy@medflow.com |
| `laboratory` | `Laboratory1234` | Laboratorista | laboratory@medflow.com |
| `cashier` | `Cashier1234` | Cajero | cashier@medflow.com |
| `vital_signs` | `VitalSigns1234` | Signos Vitales | vital_signs@medflow.com |
| `patient` | `Patient1234` | Paciente | patient@medflow.com |

## Cómo Usarlos

### Opción 1: Usar directamente en el Login

Simplemente ingresa las credenciales en el formulario de login del frontend.

### Opción 2: Usar en Código (para testing/desarrollo)

```typescript
import { validateMockCredentials, printAvailableUsers } from '../services/mockData';

// Ver todos los usuarios disponibles en consola
printAvailableUsers();

// Validar credenciales
const response = validateMockCredentials('admin', 'Admin1234');
if (response) {
  // Simular login exitoso
  localStorage.setItem('auth_token', response.token);
  localStorage.setItem('user_data', JSON.stringify(response.user));
}
```

### Opción 3: Crear un Botón de Login Rápido

Para desarrollo rápido, puedes agregar un botón de testing que loguee automáticamente:

```typescript
import { getMockAuthResponse } from '../services/mockData';

const quickLogin = (username: string) => {
  const response = getMockAuthResponse(username);
  if (response) {
    localStorage.setItem('auth_token', response.token);
    localStorage.setItem('user_data', JSON.stringify(response.user));
    window.location.reload();
  }
};
```

## Notas

- ⚠️ Estos usuarios son **SOLO PARA FRONTEND** y no existen en la base de datos
- 📝 Para cambiar usuarios, edita `src/services/mockData.ts`
- 🔐 Las contraseñas aquí son en texto plano (por eso son solo para testing)
- ✅ La función `printAvailableUsers()` imprime todo en la consola del navegador
