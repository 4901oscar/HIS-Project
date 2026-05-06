# MedFlow Frontend

MedFlow is a comprehensive healthcare management system frontend built with React, TypeScript, and Vite. This application provides a modern, accessible interface for managing patient care workflows including appointments, triage, vital signs, and consultations.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [User Roles](#user-roles)
- [Key Features](#key-features)
  - [Triage Pending List](#triage-pending-list)
  - [Vital Signs Capture](#vital-signs-capture)
  - [Doctor Consultation](#doctor-consultation)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

## Features

- **Role-Based Access Control**: Secure authentication with role-specific features (ADMIN, DOCTOR, VITAL_SIGNS, RECEPTIONIST)
- **Triage Management**: Manchester Triage System integration for patient prioritization
- **Vital Signs Recording**: Comprehensive vital signs capture with validation
- **Appointment Management**: Complete appointment lifecycle management
- **Responsive Design**: Mobile-first design that works on all devices
- **Accessibility**: WCAG 2.1 AA compliant with full keyboard navigation support
- **Real-time Updates**: Auto-refresh functionality for pending lists
- **Internationalization**: Spanish language interface

## Technology Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **Tailwind CSS** - Utility-first CSS framework
- **Heroicons** - Icon library
- **ESLint** - Code linting

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend services running (API Gateway, Clinical Service, Patient Service)

### Installation

```bash
# Clone the repository
git clone <repository-url>
cd frontend-medflow

# Install dependencies
npm install

# Copy environment variables
cp .env.example .env

# Update .env with your backend API URL
# VITE_API_BASE_URL=http://localhost:8080

# Start development server
npm run dev
```

The application will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
npm run preview  # Preview production build locally
```

## User Roles

### VITAL_SIGNS
- Access to triage pending list
- Record patient vital signs
- Perform Manchester triage assessments
- View patient information

### DOCTOR
- All VITAL_SIGNS permissions
- Access to consultation workflows
- View patient medical history
- Create prescriptions and diagnoses

### RECEPTIONIST
- Manage appointments
- Register new patients
- Update patient information

### ADMIN
- Full system access
- User management
- System configuration

## Key Features

### Triage Pending List

**Route**: `/vitals/triage`  
**Roles**: VITAL_SIGNS, DOCTOR

The Triage Pending List feature enables triage staff to efficiently manage patient assessments by providing a centralized view of all active appointments awaiting triage.

#### Workflow

1. **View Pending Appointments**
   - Displays all ACTIVE appointments without triage records
   - Auto-refreshes every 30 seconds
   - Sorted by appointment date and time (ascending)
   - Shows: Date, Time, Patient ID, Reason, Appointment ID

2. **Select Appointment**
   - Click any appointment row to select
   - Displays patient details: Name, DPI, appointment info
   - Keyboard accessible (Tab, Enter)

3. **Record Vital Signs**
   - Click "Registrar Signos Vitales" button
   - Enter required measurements:
     - Systolic pressure (50-250 mmHg)
     - Diastolic pressure (30-150 mmHg)
     - Heart rate (20-300 bpm)
     - Respiratory rate (5-60 rpm)
     - Temperature (30-45 °C)
     - Oxygen saturation (50-100%)
   - Optional: Weight, Height
   - Real-time validation with error messages

4. **Perform Triage**
   - Click "Realizar Triaje" button (enabled after vital signs recorded)
   - Select consultation motif from dropdown
   - Select applicable Manchester discriminators
   - System calculates priority level (RED, ORANGE, YELLOW, GREEN, BLUE)
   - Displays success message with assigned priority
   - Appointment automatically removed from pending list

#### Key Features

- **Auto-Refresh**: List updates every 30 seconds automatically
- **Manual Refresh**: "Actualizar" button for immediate refresh
- **Concurrent Prevention**: Handles conflicts when multiple staff triage the same appointment
- **Error Handling**: User-friendly Spanish error messages for all scenarios
- **Accessibility**: Full keyboard navigation, ARIA labels, screen reader support
- **Performance**: Optimized rendering with React.memo and useMemo

#### Screenshots

```
┌─────────────────────────────────────────────────────────────┐
│ Triaje Pendiente                          [Actualizar]      │
├─────────────────────────────────────────────────────────────┤
│ Fecha      │ Hora  │ Paciente    │ Motivo         │ ID Cita│
├────────────┼───────┼─────────────┼────────────────┼────────┤
│ 2026-04-23 │ 10:00 │ patient-123 │ Dolor de cabeza│ appt-1 │
│ 2026-04-23 │ 14:30 │ patient-456 │ Fiebre         │ appt-2 │
│ 2026-04-24 │ 09:00 │ patient-789 │ Tos persistente│ appt-3 │
└─────────────────────────────────────────────────────────────┘

[Selected Appointment - Patient Details Card]
┌─────────────────────────────────────────────────────────────┐
│ Paciente Seleccionado                          [Cancelar]   │
├─────────────────────────────────────────────────────────────┤
│ Nombre: Juan Pérez                DPI: 1234567890123       │
│ Fecha de Cita: 2026-04-23         Hora: 10:00              │
│ ID Cita: appt-001                                           │
├─────────────────────────────────────────────────────────────┤
│ [Registrar Signos Vitales]  [Realizar Triaje]              │
└─────────────────────────────────────────────────────────────┘
```

#### Error Handling

| Scenario | User Message |
|----------|--------------|
| Network error | "Error al conectar con el servidor" |
| Appointment not found | "Cita no encontrada" |
| Triage already exists | "Esta cita ya tiene triaje registrado" |
| No vital signs | "El paciente no tiene signos vitales registrados" |
| Validation error | Field-specific error messages |

### Vital Signs Capture

**Component**: `VitalSignsForm`

Comprehensive vital signs recording with:
- Real-time validation
- Range constraints enforcement
- Required and optional fields
- Responsive grid layout
- Accessibility compliant

### Doctor Consultation

**Route**: `/doctor/consultation`  
**Roles**: DOCTOR

Complete consultation workflow including:
- Patient history review
- Diagnosis entry
- Prescription management
- Treatment notes

## Project Structure

```
frontend-medflow/
├── src/
│   ├── api/              # API client configuration
│   ├── assets/           # Static assets (images, icons)
│   ├── components/       # Reusable React components
│   │   ├── common/       # Common UI components (alerts, buttons)
│   │   ├── triage/       # Triage-specific components
│   │   └── Layout/       # Layout components (MainLayout, Navigation)
│   ├── context/          # React context providers (AuthContext)
│   ├── hooks/            # Custom React hooks
│   ├── pages/            # Page components (routes)
│   │   ├── vitals/       # Vital signs and triage pages
│   │   ├── doctor/       # Doctor consultation pages
│   │   └── admin/        # Admin pages
│   ├── services/         # API service functions
│   │   ├── clinicalService.ts    # Clinical endpoints
│   │   ├── patientService.ts     # Patient endpoints
│   │   └── manchesterService.ts  # Manchester catalog
│   ├── types/            # TypeScript type definitions
│   ├── utils/            # Utility functions
│   ├── App.tsx           # Main application component
│   └── main.tsx          # Application entry point
├── public/               # Public static files
├── .env.example          # Environment variables template
├── package.json          # Dependencies and scripts
├── tailwind.config.js    # Tailwind CSS configuration
├── tsconfig.json         # TypeScript configuration
└── vite.config.ts        # Vite configuration
```

## Development

### Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run linter
npm run lint

# Run tests
npm run test

# Type check
npm run type-check
```

### Code Style

- **TypeScript**: Strict mode enabled
- **ESLint**: Configured with React and TypeScript rules
- **Prettier**: Code formatting (if configured)
- **Naming Conventions**:
  - Components: PascalCase (e.g., `TriagePendingPage.tsx`)
  - Services: camelCase (e.g., `clinicalService.ts`)
  - Types: PascalCase with descriptive names (e.g., `PendingTriageAppointment`)

### Environment Variables

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080

# Feature Flags (optional)
VITE_ENABLE_TRIAGE_PENDING=true
```

## Testing

### Unit Tests

```bash
npm run test
```

Tests are written using:
- **React Testing Library** - Component testing
- **Jest** - Test runner
- **MSW** - API mocking

### Integration Tests

```bash
npm run test:e2e
```

End-to-end tests using Cypress for complete workflow validation.

### Accessibility Testing

- Manual testing with screen readers (NVDA, JAWS)
- Keyboard navigation verification
- Color contrast validation
- ARIA attribute verification

## Deployment

### Docker Deployment

```bash
# Build Docker image
docker build -t medflow-frontend .

# Run container
docker run -p 80:80 medflow-frontend
```

### Environment-Specific Builds

```bash
# Development
npm run build

# Production
npm run build -- --mode production
```

### Nginx Configuration

The application includes an `nginx.conf` for production deployment with:
- SPA routing support
- Gzip compression
- Security headers
- API proxy configuration

## API Integration

### Backend Services

The frontend communicates with the following backend services:

- **API Gateway** (`:8080`) - Main entry point
- **Clinical Service** (`:8081`) - Triage, vital signs, appointments
- **Patient Service** (`:8082`) - Patient management
- **Auth Service** (`:8083`) - Authentication and authorization

### API Endpoints Used

#### Triage Workflow
- `GET /api/clinical/appointments/pending-triage` - Get pending appointments
- `GET /api/clinical/appointments/{id}/triage` - Get appointment triage
- `POST /api/clinical/triage` - Create triage (with appointmentId)
- `POST /api/clinical/vital-signs` - Record vital signs

#### Manchester Catalog
- `GET /api/clinical/manchester/motifs` - Get consultation motifs
- `GET /api/clinical/manchester/discriminators` - Get discriminators

#### Patient Management
- `GET /api/patients/{id}` - Get patient details
- `GET /api/patients/search` - Search patients

## Accessibility Features

- **Keyboard Navigation**: Full keyboard support (Tab, Enter, Escape, Arrow keys)
- **Screen Reader Support**: ARIA labels, roles, and live regions
- **Color Contrast**: WCAG 2.1 AA compliant (4.5:1 for normal text)
- **Focus Management**: Logical focus order and visible focus indicators
- **Semantic HTML**: Proper use of headings, landmarks, and form labels
- **Error Announcements**: Screen reader announcements for errors and success messages

## Performance Optimizations

- **Code Splitting**: Route-based code splitting with React.lazy
- **Memoization**: React.memo for expensive components
- **Debouncing**: Debounced search and refresh operations
- **Lazy Loading**: Manchester catalog loaded on-demand
- **Optimized Re-renders**: useMemo and useCallback for stable references

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Contributing

1. Create a feature branch from `main`
2. Make your changes following the code style guidelines
3. Write tests for new features
4. Ensure all tests pass
5. Submit a pull request with a clear description

## License

[Your License Here]

## Support

For issues or questions:
- Create an issue in the repository
- Contact the development team
- Refer to the API documentation

---

**Version**: 1.0.0  
**Last Updated**: April 2026
