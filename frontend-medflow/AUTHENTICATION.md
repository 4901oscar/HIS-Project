# 🔐 Sistema de Autenticación MedFlow

## ✅ Componentes Creados

### 📄 Páginas

- ✅ **LoginPage.tsx** - Vista de login para empleados
  - Formulario de autenticación con validación
  - Diseño responsive y moderno
  - Toggle para mostrar/ocultar contraseña
  - Mensajes de error
  - Credenciales demo para testing
  - Loading states

### 🔌 Servicios

- ✅ **authService.ts** - Servicio de autenticación con placeholders Axios
  - `login(credentials)` - Autenticar empleado
  - `logout()` - Cerrar sesión
  - `isAuthenticated()` - Verificar si está autenticado
  - `getCurrentUser()` - Obtener datos del usuario actual
  - `verifyToken()` - Verificar validez del token JWT
  - Mock data con 4 usuarios de prueba
  - JWT mock almacenado en localStorage

### 🎯 Context & Hooks

- ✅ **AuthContext.tsx** - Contexto global de autenticación
  - Estado global del usuario autenticado
  - Provider para toda la aplicación
  - Verificación automática de sesión al cargar

- ✅ **useAuth.ts** - Hook personalizado
  - Acceso fácil al contexto de autenticación
  - `user`, `isAuthenticated`, `isLoading`, `logout`, `setUser`

### 🛡️ Protección de Rutas

- ✅ **ProtectedRoute.tsx** - Componente para rutas protegidas
  - Verifica autenticación
  - Verifica rol del usuario
  - Redirecciona a login si no está autenticado
  - Muestra error si no tiene permisos
  - Loading state durante verificación
  - ADMINISTRATOR tiene acceso a todo

### 📚 Ejemplos

- ✅ **App.example.tsx** - Configuración completa del router
  - Integración con AuthProvider
  - Rutas públicas y protegidas
  - Protección por roles
  - Redirecciones

## 🚀 Cómo Usar

### 1. Integrar en App.tsx

```tsx
import { AuthProvider } from './context/AuthContext';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';
import LoginPage from './pages/LoginPage';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/admission"
            element={
              <ProtectedRoute requiredRole="ADMISSION">
                <AdmissionPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </Router>
    </AuthProvider>
  );
}
```

### 2. Usar el Hook en Componentes

```tsx
import { useAuth } from './hooks/useAuth';

function MiComponente() {
  const { user, isAuthenticated, logout } = useAuth();
  
  if (!isAuthenticated) {
    return <p>No autenticado</p>;
  }
  
  return (
    <div>
      <h1>Bienvenido {user.name}</h1>
      <p>Rol: {user.role}</p>
      <button onClick={logout}>Cerrar Sesión</button>
    </div>
  );
}
```

### 3. Actualizar MainLayout

```tsx
import { useAuth } from '../hooks/useAuth';

const MainLayout = ({ children }) => {
  const { user, logout } = useAuth();
  
  return (
    <MainLayout userRole={user?.role} userName={user?.name}>
      {children}
    </MainLayout>
  );
};
```

## 🔐 Credenciales Demo

Para testing y desarrollo:

| Usuario | Password | Rol |
|---------|----------|-----|
| admin | password | ADMINISTRATOR |
| admission | password | ADMISSION |
| doctor | password | DOCTOR |
| nurse | password | VITAL_SIGNS |

## 🎨 Características del Login

- ✅ **Diseño moderno** con gradientes y efectos visuales
- ✅ **Formulario validado** con campos requeridos
- ✅ **Toggle password** para mostrar/ocultar contraseña
- ✅ **Remember me** checkbox
- ✅ **Forgot password** link (pendiente implementar)
- ✅ **Loading states** durante autenticación
- ✅ **Mensajes de error** claros y visibles
- ✅ **Responsive design** mobile-first
- ✅ **Credenciales demo** visibles para testing
- ✅ **Branding MedFlow** consistente

## 🛡️ Seguridad

- ✅ **JWT Token** almacenado en localStorage (mock)
- ✅ **Verificación automática** de sesión al cargar
- ✅ **Protección por roles** en cada ruta
- ✅ **ADMINISTRATOR** tiene acceso universal
- ✅ **Logout** limpia localStorage
- ✅ **Token verification** ready para backend

## 🔌 Integración Backend

Cuando el backend esté listo, descomentar en `authService.ts`:

```typescript
// BACKEND READY
const response = await axios.post(`${API_URL}/auth/login`, credentials);
const { token, user } = response.data;

localStorage.setItem('auth_token', token);
localStorage.setItem('user_data', JSON.stringify(user));

return { token, user };
```

## 📊 Estructura de Datos

### LoginCredentials
```typescript
{
  username: string;
  password: string;
}
```

### AuthResponse
```typescript
{
  token: string;  // JWT token
  user: {
    id: string;
    username: string;
    name: string;
    role: string;  // ADMINISTRATOR, ADMISSION, DOCTOR, etc.
    email: string;
  }
}
```

## 🎯 Roles Disponibles

- `ADMINISTRATOR` - Acceso total al sistema
- `ADMISSION` - Gestión de citas
- `VITAL_SIGNS` - Captura de signos vitales (Enfermería)
- `DOCTOR` - Consultas y triaje
- `LABORATORY` - Gestión de muestras
- `PHARMACY` - Despacho de medicinas
- `CASHIER` - Caja y facturación

## ✨ Próximos Pasos

1. Copiar `App.example.tsx` a `App.tsx`
2. Configurar rutas según necesidad
3. Implementar "Forgot Password" funcionalidad
4. Conectar con backend cuando esté disponible
5. Implementar refresh token
6. Agregar 2FA (opcional)
