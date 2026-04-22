import { useState, useEffect } from 'react';
import type { FC, FormEvent } from 'react';
import { MainLayout } from '../../components/Layout';
import {
  MagnifyingGlassIcon,
  UserPlusIcon,
  CheckCircleIcon,
  CalendarDaysIcon,
} from '@heroicons/react/24/outline';
import { createPatient, searchPatients } from '../../services/patientService';
import type { PatientResponse, CreatePatientRequest } from '../../services/patientService';
import { activateAppointment, listAllAppointments } from '../../services/appointmentService';
import type { AppointmentResponse } from '../../services/appointmentService';
import axios from 'axios';

type Tab = 'register' | 'activate' | 'list';

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

const emptyForm: CreatePatientRequest = {
  dpi: '',
  nit: '',
  firstName: '',
  secondName: '',
  firstLastName: '',
  secondLastName: '',
  birthDate: '',
  gender: 'MALE',
  email: '',
  phone: '',
  department: '',
  municipality: '',
  zone: '',
  address: '',
};

const ActivateAppointments: FC = () => {
  const [tab, setTab] = useState<Tab>('register');

  // ── Lista de citas ────────────────────────────────────────────────────────
  const [allAppointments, setAllAppointments] = useState<AppointmentResponse[]>([]);
  const [listLoading, setListLoading] = useState(false);
  const [listLoaded, setListLoaded] = useState(false);

  useEffect(() => {
    if (tab === 'list' && !listLoaded) {
      setListLoading(true);
      listAllAppointments()
        .then(setAllAppointments)
        .catch(() => {})
        .finally(() => { setListLoading(false); setListLoaded(true); });
    }
  }, [tab, listLoaded]);

  // ── Registro de paciente ──────────────────────────────────────────────────
  const [form, setForm] = useState<CreatePatientRequest>(emptyForm);
  const [regLoading, setRegLoading] = useState(false);
  const [regSuccess, setRegSuccess] = useState<PatientResponse | null>(null);
  const [regError, setRegError] = useState<string | null>(null);

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();
    setRegLoading(true);
    setRegError(null);
    setRegSuccess(null);
    try {
      const patient = await createPatient(form);
      setRegSuccess(patient);
      setForm(emptyForm);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const msg = err.response?.data?.message || err.response?.data?.error || 'Error al registrar paciente';
        setRegError(msg);
      } else {
        setRegError('Error al conectar con el servidor');
      }
    } finally {
      setRegLoading(false);
    }
  };

  // ── Activar cita ──────────────────────────────────────────────────────────
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<PatientResponse[]>([]);
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null);
  const [appointmentId, setAppointmentId] = useState('');
  const [actLoading, setActLoading] = useState(false);
  const [actSuccess, setActSuccess] = useState<string | null>(null);
  const [actError, setActError] = useState<string | null>(null);
  const [searching, setSearching] = useState(false);

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;
    setSearching(true);
    setSearchResults([]);
    setSelectedPatient(null);
    try {
      const results = await searchPatients(searchQuery);
      setSearchResults(results);
    } catch {
      setActError('Error al buscar paciente');
    } finally {
      setSearching(false);
    }
  };

  const handleActivate = async (e: FormEvent) => {
    e.preventDefault();
    if (!appointmentId.trim()) return;
    setActLoading(true);
    setActError(null);
    setActSuccess(null);
    try {
      await activateAppointment(appointmentId.trim());
      setActSuccess(`Cita ${appointmentId.trim()} activada exitosamente`);
      setAppointmentId('');
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const msg = err.response?.data?.message || 'Error al activar la cita';
        setActError(msg);
      } else {
        setActError('Error al conectar con el servidor');
      }
    } finally {
      setActLoading(false);
    }
  };

  const inputClass =
    'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Admisión</h2>
          <p className="mt-1 text-sm text-gray-600">Registro de pacientes y activación de citas</p>
        </div>

        {/* Tabs */}
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setTab('register')}
              className={`py-3 px-1 border-b-2 font-medium text-sm flex items-center gap-2 ${
                tab === 'register'
                  ? 'border-medin-cyan text-medin-cyan'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              <UserPlusIcon className="h-4 w-4" />
              Registrar Paciente
            </button>
            <button
              onClick={() => setTab('activate')}
              className={`py-3 px-1 border-b-2 font-medium text-sm flex items-center gap-2 ${
                tab === 'activate'
                  ? 'border-medin-cyan text-medin-cyan'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              <CheckCircleIcon className="h-4 w-4" />
              Activar Cita
            </button>
            <button
              onClick={() => setTab('list')}
              className={`py-3 px-1 border-b-2 font-medium text-sm flex items-center gap-2 ${
                tab === 'list'
                  ? 'border-medin-cyan text-medin-cyan'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              <CalendarDaysIcon className="h-4 w-4" />
              Ver Citas
            </button>
          </nav>
        </div>

        {/* ── Tab: Registrar Paciente ── */}
        {tab === 'register' && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Datos del Paciente</h3>

            {regSuccess && (
              <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-green-800 font-medium">Paciente registrado exitosamente</p>
                <p className="text-green-700 text-sm mt-1">
                  ID: <span className="font-mono">{regSuccess.id}</span> — {regSuccess.fullName}
                </p>
                <p className="text-green-700 text-sm">DPI: {regSuccess.dpi}</p>
              </div>
            )}

            {regError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">
                {regError}
              </div>
            )}

            <form onSubmit={handleRegister} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className={labelClass}>DPI <span className="text-red-500">*</span></label>
                  <input name="dpi" value={form.dpi} onChange={handleFormChange} required maxLength={13} minLength={13} placeholder="13 dígitos" className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>NIT</label>
                  <input name="nit" value={form.nit} onChange={handleFormChange} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Primer Nombre <span className="text-red-500">*</span></label>
                  <input name="firstName" value={form.firstName} onChange={handleFormChange} required className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Segundo Nombre</label>
                  <input name="secondName" value={form.secondName} onChange={handleFormChange} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Primer Apellido <span className="text-red-500">*</span></label>
                  <input name="firstLastName" value={form.firstLastName} onChange={handleFormChange} required className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Segundo Apellido</label>
                  <input name="secondLastName" value={form.secondLastName} onChange={handleFormChange} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Fecha de Nacimiento <span className="text-red-500">*</span></label>
                  <input type="date" name="birthDate" value={form.birthDate} onChange={handleFormChange} required className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Género <span className="text-red-500">*</span></label>
                  <select name="gender" value={form.gender} onChange={handleFormChange} required className={inputClass}>
                    <option value="MALE">Masculino</option>
                    <option value="FEMALE">Femenino</option>
                    <option value="OTHER">Otro</option>
                  </select>
                </div>
                <div>
                  <label className={labelClass}>Correo Electrónico <span className="text-red-500">*</span></label>
                  <input type="email" name="email" value={form.email} onChange={handleFormChange} required className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Teléfono <span className="text-red-500">*</span></label>
                  <input name="phone" value={form.phone} onChange={handleFormChange} required maxLength={8} minLength={8} placeholder="8 dígitos" className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Departamento</label>
                  <input name="department" value={form.department} onChange={handleFormChange} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Municipio</label>
                  <input name="municipality" value={form.municipality} onChange={handleFormChange} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Zona</label>
                  <input name="zone" value={form.zone} onChange={handleFormChange} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Dirección</label>
                  <input name="address" value={form.address} onChange={handleFormChange} className={inputClass} />
                </div>
              </div>

              <div className="flex justify-end pt-2">
                <button
                  type="submit"
                  disabled={regLoading}
                  className="px-6 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50"
                >
                  {regLoading ? 'Registrando...' : 'Registrar Paciente'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* ── Tab: Ver Citas ── */}
        {tab === 'list' && (
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Todas las Citas</h3>
              <button
                onClick={() => { setListLoaded(false); }}
                className="text-xs text-medin-cyan hover:underline"
              >
                Actualizar
              </button>
            </div>
            {listLoading ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-7 w-7 border-4 border-medin-cyan border-t-transparent"></div>
              </div>
            ) : allAppointments.length === 0 ? (
              <p className="text-gray-500 text-sm">No hay citas registradas.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
                      <th className="pb-2 pr-4">Fecha</th>
                      <th className="pb-2 pr-4">Hora</th>
                      <th className="pb-2 pr-4">Estado</th>
                      <th className="pb-2 pr-4">Motivo</th>
                      <th className="pb-2 pr-4">ID Paciente</th>
                      <th className="pb-2">ID Cita</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {allAppointments
                      .slice()
                      .sort((a, b) => a.appointmentDate.localeCompare(b.appointmentDate) || a.appointmentTime.localeCompare(b.appointmentTime))
                      .map((appt) => (
                        <tr key={appt.id} className="hover:bg-gray-50">
                          <td className="py-2 pr-4 whitespace-nowrap">{appt.appointmentDate}</td>
                          <td className="py-2 pr-4 whitespace-nowrap">{appt.appointmentTime.substring(0, 5)}</td>
                          <td className="py-2 pr-4">
                            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[appt.status] ?? 'bg-gray-100 text-gray-700'}`}>
                              {STATUS_LABEL[appt.status] ?? appt.status}
                            </span>
                          </td>
                          <td className="py-2 pr-4 max-w-xs truncate">{appt.notes ?? '—'}</td>
                          <td className="py-2 pr-4 font-mono text-xs">{appt.patientId}</td>
                          <td className="py-2 font-mono text-xs">{appt.id}</td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* ── Tab: Activar Cita ── */}
        {tab === 'activate' && (
          <div className="space-y-4">
            {/* Buscar paciente */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Buscar Paciente</h3>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <MagnifyingGlassIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <input
                    type="text"
                    placeholder="Buscar por nombre, DPI o correo..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm"
                  />
                </div>
                <button
                  onClick={handleSearch}
                  disabled={searching}
                  className="px-4 py-2 bg-medin-navy text-white rounded-lg hover:bg-medin-navy/90 text-sm disabled:opacity-50"
                >
                  {searching ? 'Buscando...' : 'Buscar'}
                </button>
              </div>

              {searchResults.length > 0 && (
                <div className="mt-3 divide-y divide-gray-100 border border-gray-200 rounded-lg overflow-hidden">
                  {searchResults.map((p) => (
                    <button
                      key={p.id}
                      onClick={() => { setSelectedPatient(p); setSearchResults([]); }}
                      className="w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors"
                    >
                      <p className="font-medium text-gray-900 text-sm">{p.fullName}</p>
                      <p className="text-xs text-gray-500">DPI: {p.dpi} — {p.email}</p>
                    </button>
                  ))}
                </div>
              )}

              {selectedPatient && (
                <div className="mt-3 p-3 bg-medin-cyan/10 border border-medin-cyan/30 rounded-lg">
                  <p className="font-medium text-gray-900 text-sm">{selectedPatient.fullName}</p>
                  <p className="text-xs text-gray-600">DPI: {selectedPatient.dpi}</p>
                  <p className="text-xs text-gray-600">ID: <span className="font-mono">{selectedPatient.id}</span></p>
                </div>
              )}
            </div>

            {/* Activar cita */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Activar Cita por ID</h3>

              {actSuccess && (
                <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-800 text-sm">
                  {actSuccess}
                </div>
              )}
              {actError && (
                <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">
                  {actError}
                </div>
              )}

              <form onSubmit={handleActivate} className="flex gap-2">
                <input
                  type="text"
                  placeholder="ID de la cita (UUID)"
                  value={appointmentId}
                  onChange={(e) => setAppointmentId(e.target.value)}
                  required
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm font-mono"
                />
                <button
                  type="submit"
                  disabled={actLoading}
                  className="px-5 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm disabled:opacity-50 flex items-center gap-1"
                >
                  <CheckCircleIcon className="h-4 w-4" />
                  {actLoading ? 'Activando...' : 'Activar'}
                </button>
              </form>
            </div>
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default ActivateAppointments;
