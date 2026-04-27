/**
 * App.example.tsx - Ejemplo de configuración del router con autenticación
 * Copia este contenido a tu App.tsx principal
 */

import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';

// Importar páginas por rol
import AdministratorDashboard from './pages/administrator/AdministratorDashboard';
import ActivateAppointments from './pages/admission/ActivateAppointments';
import TriagePendingPage from './pages/vitals/TriagePendingPage';
import TriageVitalSignsCapture from './pages/vitals/TriageVitalSignsCapture';
import DoctorConsultation from './pages/doctor/DoctorConsultation';
import LabSampleManagement from './pages/lab/LabSampleManagement';
import PharmacyDispense from './pages/pharmacy/PharmacyDispense';
import CashierPage from './pages/cashier/CashierPage';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Ruta pública - Login */}
          <Route path="/login" element={<LoginPage />} />

          {/* Rutas protegidas */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />

          {/* Administrator - Solo ADMINISTRATOR */}
          <Route
            path="/administrator"
            element={
              <ProtectedRoute requiredRole="ADMINISTRATOR">
                <AdministratorDashboard />
              </ProtectedRoute>
            }
          />

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

          {/* Redirecciones */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
