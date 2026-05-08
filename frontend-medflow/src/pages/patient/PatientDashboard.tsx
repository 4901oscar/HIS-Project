import { useState, useEffect } from 'react';
import type { FC } from 'react';
import axios from 'axios';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { getPatientByDpi, updatePatient } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { listMyAppointments } from '../../services/appointmentService';
import type { AppointmentResponse } from '../../services/appointmentService';
import PatientHeader from '../../components/PatientHeader/PatientHeader';
import AppointmentDetailModal from '../../components/patient/AppointmentDetailModal';
import HistorialFloatingButton from '../../components/shared/HistorialFloatingButton';
import { usePatientHistory } from '../../context/PatientHistoryContext';

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Agendada',
  ACTIVE: 'Activa',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
};
const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

const PatientDashboard: FC = () => {
  const { user } = useAuth();
  const location = useLocation();
  const { setPatient } = usePatientHistory();

  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [apptLoading, setApptLoading] = useState(true);
  const [selectedAppt, setSelectedAppt] = useState<AppointmentResponse | null>(null);

  const [showProfile, setShowProfile] = useState(
    (location.state as { openProfile?: boolean })?.openProfile === true
  );
  const [patient, setPatientData] = useState<PatientResponse | null>(null);
  const [profileForm, setProfileForm] = useState({
    firstName: '', secondName: '', firstLastName: '', secondLastName: '',
    phone: '', address: '', department: '', municipality: '',
  });
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSuccess, setProfileSuccess] = useState(false);

  useEffect(() => {
    listMyAppointments()
      .then(setAppointments)
      .catch(() => {})
      .finally(() => setApptLoading(false));
  }, []);

  useEffect(() => {
    if (!user?.username) return;
    getPatientByDpi(user.username)
      .then((pat) => {
        setPatientData(pat);
        setProfileForm({
          firstName: pat.firstName ?? '',
          secondName: pat.secondName ?? '',
          firstLastName: pat.firstLastName ?? '',
          secondLastName: pat.secondLastName ?? '',
          phone: pat.phone ?? '',
          address: pat.address ?? '',
          department: pat.department ?? '',
          municipality: pat.municipality ?? '',
        });
        const fullName = [pat.firstName, pat.firstLastName].filter(Boolean).join(' ');
        setPatient(pat.id, fullName);
      })
      .catch(() => {});
  }, [user, setPatient]);

  const handleProfileSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!patient) return;
    setProfileSaving(true);
    setProfileError(null);
    setProfileSuccess(false);
    try {
      const updated = await updatePatient(patient.id, profileForm);
      setPatientData(updated);
      setProfileSuccess(true);
      setTimeout(() => setProfileSuccess(false), 3000);
    } catch {
      setProfileError('No se pudo guardar. Intenta de nuevo.');
    } finally {
      setProfileSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <PatientHeader onProfileClick={() => setShowProfile(true)} />

      <main className="flex-1 max-w-4xl mx-auto w-full p-6">
        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Mis Citas</h2>

        <div className="space-y-3">
          {apptLoading ? (
            <div className="text-center py-8">
              <div className="inline-block animate-spin rounded-full h-7 w-7 border-4 border-medin-cyan border-t-transparent"></div>
            </div>
          ) : appointments.length === 0 ? (
            <p className="text-gray-500 text-sm">No tienes citas registradas.</p>
          ) : (
            appointments
              .slice()
              .sort((a, b) => b.appointmentDate.localeCompare(a.appointmentDate) || b.appointmentTime.localeCompare(a.appointmentTime))
              .map((appt) => (
                <div key={appt.id} className="bg-white rounded-lg shadow p-4 flex items-start justify-between gap-4">
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">
                      {appt.appointmentDate} — {appt.appointmentTime.substring(0, 5)}
                    </p>
                    {appt.notes && <p className="text-sm text-gray-600 mt-1">{appt.notes}</p>}
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[appt.status] ?? 'bg-gray-100 text-gray-700'}`}>
                      {STATUS_LABEL[appt.status] ?? appt.status}
                    </span>
                    <button
                      onClick={() => setSelectedAppt(appt)}
                      className="text-sm font-medium text-medin-cyan hover:text-medin-blue transition-colors"
                    >
                      Ver
                    </button>
                  </div>
                </div>
              ))
          )}
        </div>
      </main>

      <HistorialFloatingButton />

      {selectedAppt && (
        <AppointmentDetailModal
          appointmentId={selectedAppt.id}
          appointmentDate={selectedAppt.appointmentDate}
          appointmentTime={selectedAppt.appointmentTime}
          status={selectedAppt.status}
          notes={selectedAppt.notes}
          onClose={() => setSelectedAppt(null)}
        />
      )}

      {/* Profile Modal */}
      {showProfile && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
              <h3 className="text-xl font-semibold text-gray-900">Mi Perfil</h3>
              <button onClick={() => setShowProfile(false)} className="text-gray-400 hover:text-gray-600">
                <XMarkIcon className="h-6 w-6" />
              </button>
            </div>
            <form onSubmit={handleProfileSave} className="p-6 space-y-4">
              {profileError && (
                <div className="p-3 bg-red-50 border border-red-200 rounded text-red-800 text-sm">{profileError}</div>
              )}
              {profileSuccess && (
                <div className="p-3 bg-green-50 border border-green-200 rounded text-green-800 text-sm">Perfil actualizado correctamente</div>
              )}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">DPI</label>
                  <input type="text" value={patient?.dpi ?? ''} disabled className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-50 text-gray-500 cursor-not-allowed" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Correo electronico</label>
                  <input type="text" value={patient?.email ?? ''} disabled className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-50 text-gray-500 cursor-not-allowed" />
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Primer nombre <span className="text-red-500">*</span></label>
                  <input type="text" required value={profileForm.firstName}
                    onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Segundo nombre</label>
                  <input type="text" value={profileForm.secondName}
                    onChange={(e) => setProfileForm({ ...profileForm, secondName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Primer apellido <span className="text-red-500">*</span></label>
                  <input type="text" required value={profileForm.firstLastName}
                    onChange={(e) => setProfileForm({ ...profileForm, firstLastName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Segundo apellido</label>
                  <input type="text" value={profileForm.secondLastName}
                    onChange={(e) => setProfileForm({ ...profileForm, secondLastName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Telefono <span className="text-red-500">*</span></label>
                <input type="tel" required value={profileForm.phone}
                  onChange={(e) => setProfileForm({ ...profileForm, phone: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Direccion</label>
                <input type="text" value={profileForm.address}
                  onChange={(e) => setProfileForm({ ...profileForm, address: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Departamento</label>
                  <input type="text" value={profileForm.department}
                    onChange={(e) => setProfileForm({ ...profileForm, department: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Municipio</label>
                  <input type="text" value={profileForm.municipality}
                    onChange={(e) => setProfileForm({ ...profileForm, municipality: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
              </div>
              <div className="flex justify-end space-x-3 pt-4">
                <button type="button" onClick={() => setShowProfile(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors">
                  Cancelar
                </button>
                <button type="submit" disabled={profileSaving}
                  className="px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50">
                  {profileSaving ? 'Guardando...' : 'Guardar cambios'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default PatientDashboard;
