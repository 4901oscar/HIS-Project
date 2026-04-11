import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';
import './App.css';

// Páginas públicas
import HomePage from './pages/HomePage';
import AppointmentPage from './pages/AppointmentPage';
import LoginPage from './pages/LoginPage';

// Páginas protegidas
import DashboardPage from './pages/DashboardPage';
import AdministratorDashboard from './pages/administrator/AdministratorDashboard';
import ActivateAppointments from './pages/admission/ActivateAppointments';
import VitalSignsCapture from './pages/vitals/VitalSignsCapture';
import DoctorConsultation from './pages/doctor/DoctorConsultation';
import LabSampleManagement from './pages/lab/LabSampleManagement';
import PharmacyDispense from './pages/pharmacy/PharmacyDispense';
import CashierBilling from './pages/cashier/CashierBilling';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Rutas Públicas */}
          <Route path="/" element={<HomePage />} />
          <Route path="/appointment" element={<AppointmentPage />} />
          <Route path="/login" element={<LoginPage />} />

          {/* Dashboard - Todas las rutas protegidas */}
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

          {/* Vital Signs - VITAL_SIGNS y ADMINISTRATOR */}
          <Route
            path="/vitals"
            element={
              <ProtectedRoute requiredRole="VITAL_SIGNS">
                <VitalSignsCapture />
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
                <CashierBilling />
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
