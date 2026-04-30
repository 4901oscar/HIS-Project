import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';
import './App.css';

// Páginas públicas
import HomePage from './pages/HomePage';
import Nosotros from './pages/Nosotros';
import Servicios from './pages/Servicios';

import AppointmentPage from './pages/AppointmentPage';
import PaymentGatewayPage from './pages/PaymentGatewayPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ActivateAccountPage from './pages/ActivateAccountPage';

// Páginas protegidas
import DashboardPage from './pages/DashboardPage';
import AdministratorDashboard from './pages/administrator/AdministratorDashboard';
import EmployeeManagementPage from './pages/administrator/EmployeeManagementPage';
import EmployeeFormPage from './pages/administrator/EmployeeFormPage';
import DoctorManagementPage from './pages/administrator/DoctorManagementPage';
import MedicamentosPage from './pages/administrator/MedicamentosPage';
import ExamenesPage from './pages/administrator/ExamenesPage';
import ServiciosPage from './pages/administrator/ServiciosPage';
import TriageCatalogPage from './pages/administrator/TriageCatalogPage';
import ActivateAppointments from './pages/admission/ActivateAppointments';
import TriagePendingPage from './pages/vitals/TriagePendingPage';
import TriageVitalSignsCapture from './pages/vitals/TriageVitalSignsCapture';
import DoctorConsultation from './pages/doctor/DoctorConsultation';
import LabSampleManagement from './pages/lab/LabSampleManagement';
import PharmacyDispense from './pages/pharmacy/PharmacyDispense';
import CashierPage from './pages/cashier/CashierPage';
import PatientDashboard from './pages/patient/PatientDashboard';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Rutas Públicas */}
          <Route path="/" element={<HomePage />} />
          <Route path="/nosotros" element={<Nosotros />} />
          <Route path="/servicios" element={<Servicios />} />
          <Route path="/appointment" element={<AppointmentPage />} />
          <Route path="/payment" element={<PaymentGatewayPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/activate" element={<ActivateAccountPage />} />

          {/* Dashboard - Todas las rutas protegidas */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />

          {/* Administrator - Solo ADMIN */}
          <Route
            path="/administrator"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdministratorDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/administrator/empleados"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <EmployeeManagementPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/administrator/empleados/nuevo"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <EmployeeFormPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/administrator/empleados/:id/editar"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <EmployeeFormPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/administrator/doctores"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <DoctorManagementPage />
              </ProtectedRoute>
            }
          />
          <Route path="/administrator/medicamentos" element={<ProtectedRoute requiredRole="ADMIN"><MedicamentosPage /></ProtectedRoute>} />
          <Route path="/administrator/examenes" element={<ProtectedRoute requiredRole="ADMIN"><ExamenesPage /></ProtectedRoute>} />
          <Route path="/administrator/servicios" element={<ProtectedRoute requiredRole="ADMIN"><ServiciosPage /></ProtectedRoute>} />
          <Route path="/administrator/triage" element={<ProtectedRoute requiredRole="ADMIN"><TriageCatalogPage /></ProtectedRoute>} />

          {/* Admission - ADMISSION y ADMINISTRATOR */}
          <Route
            path="/admission"
            element={
              <ProtectedRoute requiredRole="ADMISSION">
                <ActivateAppointments />
              </ProtectedRoute>
            }
          />

          {/* Triage Pending - VITAL_SIGNS, DOCTOR y ADMINISTRATOR */}
          <Route
            path="/vitals/triage"
            element={
              <ProtectedRoute requiredRole={["VITAL_SIGNS", "DOCTOR"]}>
                <TriagePendingPage />
              </ProtectedRoute>
            }
          />

          {/* Triage Vital Signs Capture - VITAL_SIGNS, DOCTOR y ADMINISTRATOR */}
          <Route
            path="/vitals/triage/capture"
            element={
              <ProtectedRoute requiredRole={["VITAL_SIGNS", "DOCTOR"]}>
                <TriageVitalSignsCapture />
              </ProtectedRoute>
            }
          />

          {/* Doctor - DOCTOR y ADMINISTRATOR */}
          <Route
            path="/doctor"
            element={
              <ProtectedRoute requiredRole="DOCTOR">
                <DoctorConsultation />
              </ProtectedRoute>
            }
          />

          {/* Laboratory - LABORATORY y ADMINISTRATOR */}
          <Route
            path="/lab"
            element={
              <ProtectedRoute requiredRole="LABORATORY">
                <LabSampleManagement />
              </ProtectedRoute>
            }
          />

          {/* Pharmacy - PHARMACY y ADMINISTRATOR */}
          <Route
            path="/pharmacy"
            element={
              <ProtectedRoute requiredRole="PHARMACY">
                <PharmacyDispense />
              </ProtectedRoute>
            }
          />

          {/* Cashier - CASHIER y ADMINISTRATOR */}
          <Route
            path="/cashier"
            element={
              <ProtectedRoute requiredRole="CASHIER">
                <CashierPage />
              </ProtectedRoute>
            }
          />

          {/* Patient - Solo PATIENT */}
          <Route
            path="/patient"
            element={
              <ProtectedRoute requiredRole="PATIENT">
                <PatientDashboard />
              </ProtectedRoute>
            }
          />

          {/* Ruta por defecto */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
