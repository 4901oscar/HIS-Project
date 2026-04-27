# Reglas Frontend React

## Stack
- React 18 sin TypeScript
- Vite como build tool
- Tailwind CSS para estilos (no CSS modules, no styled-components)

## Convenciones
- Componentes funcionales con hooks, nunca clases
- Servicios Axios con capa de abstracción y toggle USE_MOCK
- Nombrar componentes en PascalCase, archivos igual
- Un componente por archivo

## Estructura de servicios API
- Todos los servicios apuntan al API Gateway (puerto 8080)
- Cada servicio tiene su mock para desarrollo sin backend
- Cambiar USE_MOCK a false cuando el backend esté conectado

## Estilos
- Solo clases de Tailwind, no CSS custom salvo excepciones justificadas
- Responsive mobile-first