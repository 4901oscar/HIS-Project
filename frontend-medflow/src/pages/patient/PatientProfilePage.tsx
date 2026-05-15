import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { getPatientByDpi, updatePatient } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { changePassword } from '../../services/authService';
import { Navbar } from '../../components';

const inputCls = (error?: string) =>
  `w-full px-3 py-2.5 border ${error ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm`;

const PatientProfilePage: FC = () => {
  const { user } = useAuth();

  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const [profileForm, setProfileForm] = useState({
    firstName: '', secondName: '', firstLastName: '', secondLastName: '',
    phone: '', address: '', department: '', municipality: '',
  });
  const [profileErrors, setProfileErrors] = useState<Record<string, string>>({});
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);

  const [pwForm, setPwForm] = useState({ current: '', next: '', confirm: '' });
  const [pwErrors, setPwErrors] = useState<Record<string, string>>({});
  const [pwSaving, setPwSaving] = useState(false);
  const [pwSuccess, setPwSuccess] = useState(false);
  const [pwError, setPwError] = useState<string | null>(null);
  const [showPw, setShowPw] = useState({ current: false, next: false, confirm: false });

  useEffect(() => {
    if (!user?.username) return;
    getPatientByDpi(user.username)
      .then((pat) => {
        setPatient(pat);
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
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [user]);

  const handleProfileSave = async (e: { preventDefault: () => void }) => {
    e.preventDefault();
    const errors: Record<string, string> = {};
    if (!profileForm.firstName.trim()) errors.firstName = 'Requerido';
    if (!profileForm.firstLastName.trim()) errors.firstLastName = 'Requerido';
    if (!profileForm.phone.trim()) errors.phone = 'Requerido';
    if (Object.keys(errors).length > 0) { setProfileErrors(errors); return; }
    setProfileErrors({});
    setProfileSaving(true);
    setProfileError(null);
    setProfileSuccess(false);
    try {
      const updated = await updatePatient(patient!.id, profileForm);
      setPatient(updated);
      setProfileSuccess(true);
      setTimeout(() => setProfileSuccess(false), 3000);
    } catch {
      setProfileError('No se pudo guardar. Intenta de nuevo.');
    } finally {
      setProfileSaving(false);
    }
  };

  const handlePasswordSave = async (e: { preventDefault: () => void }) => {
    e.preventDefault();
    const errors: Record<string, string> = {};
    if (!pwForm.current.trim()) errors.current = 'Ingresa tu contraseña actual';
    if (pwForm.next.length < 8) errors.next = 'Mínimo 8 caracteres';
    if (pwForm.next !== pwForm.confirm) errors.confirm = 'Las contraseñas no coinciden';
    if (Object.keys(errors).length > 0) { setPwErrors(errors); return; }
    setPwErrors({});
    setPwSaving(true);
    setPwError(null);
    setPwSuccess(false);
    try {
      await changePassword(pwForm.current, pwForm.next);
      setPwSuccess(true);
      setPwForm({ current: '', next: '', confirm: '' });
      setTimeout(() => setPwSuccess(false), 3000);
    } catch {
      setPwError('No se pudo cambiar la contraseña. Verifica tu contraseña actual e intenta de nuevo.');
    } finally {
      setPwSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-5xl mx-auto w-full p-6">
        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-gray-500 mb-6">
          <Link to="/patient" className="hover:text-medin-cyan transition-colors">Mis Citas</Link>
          <span>/</span>
          <span className="text-gray-800 font-medium">Mi Perfil</span>
        </div>

        <h2 className="text-2xl font-semibold text-gray-800 mb-8">Mi Perfil</h2>

        {loading ? (
          <div className="text-center py-16">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

            {/* ── Datos Personales ── */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
              <h3 className="text-base font-semibold text-gray-900 mb-5 flex items-center gap-2">
                <svg className="w-5 h-5 text-medin-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                Datos Personales
              </h3>

              {profileError && (
                <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{profileError}</div>
              )}
              {profileSuccess && (
                <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-800 text-sm">Perfil actualizado correctamente.</div>
              )}

              <form onSubmit={handleProfileSave} className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">DPI</label>
                    <input type="text" value={patient?.dpi ?? ''} disabled
                      className="w-full px-3 py-2.5 border border-gray-200 rounded-lg bg-gray-50 text-gray-400 text-sm cursor-not-allowed" />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">NIT</label>
                    <input type="text" value={patient?.nit ?? '—'} disabled
                      className="w-full px-3 py-2.5 border border-gray-200 rounded-lg bg-gray-50 text-gray-400 text-sm cursor-not-allowed" />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">Fecha de nacimiento</label>
                    <input type="text" value={patient?.birthDate ? new Date(patient.birthDate + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: '2-digit', year: 'numeric' }) : ''} disabled
                      className="w-full px-3 py-2.5 border border-gray-200 rounded-lg bg-gray-50 text-gray-400 text-sm cursor-not-allowed" />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600 mb-1">Género</label>
                    <input type="text" value={patient?.gender === 'M' ? 'Masculino' : patient?.gender === 'F' ? 'Femenino' : patient?.gender ?? ''} disabled
                      className="w-full px-3 py-2.5 border border-gray-200 rounded-lg bg-gray-50 text-gray-400 text-sm cursor-not-allowed" />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Correo electrónico</label>
                  <input type="text" value={patient?.email ?? ''} disabled
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-lg bg-gray-50 text-gray-400 text-sm cursor-not-allowed" />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Primer nombre <span className="text-red-500">*</span></label>
                    <input type="text" value={profileForm.firstName} required
                      onChange={e => { setProfileForm(f => ({ ...f, firstName: e.target.value })); setProfileErrors(er => ({ ...er, firstName: '' })); }}
                      className={inputCls(profileErrors.firstName)} />
                    {profileErrors.firstName && <p className="mt-1 text-xs text-red-600">{profileErrors.firstName}</p>}
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Segundo nombre</label>
                    <input type="text" value={profileForm.secondName}
                      onChange={e => setProfileForm(f => ({ ...f, secondName: e.target.value }))}
                      className={inputCls()} />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Primer apellido <span className="text-red-500">*</span></label>
                    <input type="text" value={profileForm.firstLastName} required
                      onChange={e => { setProfileForm(f => ({ ...f, firstLastName: e.target.value })); setProfileErrors(er => ({ ...er, firstLastName: '' })); }}
                      className={inputCls(profileErrors.firstLastName)} />
                    {profileErrors.firstLastName && <p className="mt-1 text-xs text-red-600">{profileErrors.firstLastName}</p>}
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Segundo apellido</label>
                    <input type="text" value={profileForm.secondLastName}
                      onChange={e => setProfileForm(f => ({ ...f, secondLastName: e.target.value }))}
                      className={inputCls()} />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">Teléfono <span className="text-red-500">*</span></label>
                  <input type="tel" value={profileForm.phone} required
                    onChange={e => { setProfileForm(f => ({ ...f, phone: e.target.value })); setProfileErrors(er => ({ ...er, phone: '' })); }}
                    className={inputCls(profileErrors.phone)} />
                  {profileErrors.phone && <p className="mt-1 text-xs text-red-600">{profileErrors.phone}</p>}
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">Dirección</label>
                  <input type="text" value={profileForm.address}
                    onChange={e => setProfileForm(f => ({ ...f, address: e.target.value }))}
                    className={inputCls()} />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Departamento</label>
                    <input type="text" value={profileForm.department}
                      onChange={e => setProfileForm(f => ({ ...f, department: e.target.value }))}
                      className={inputCls()} />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Municipio</label>
                    <input type="text" value={profileForm.municipality}
                      onChange={e => setProfileForm(f => ({ ...f, municipality: e.target.value }))}
                      className={inputCls()} />
                  </div>
                </div>

                <div className="pt-2">
                  <button type="submit" disabled={profileSaving}
                    className="w-full py-2.5 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 text-sm">
                    {profileSaving ? 'Guardando...' : 'Guardar cambios'}
                  </button>
                </div>
              </form>
            </div>

            {/* ── Cambiar Contraseña ── */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
              <h3 className="text-base font-semibold text-gray-900 mb-5 flex items-center gap-2">
                <svg className="w-5 h-5 text-medin-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
                Cambiar Contraseña
              </h3>

              {pwError && (
                <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{pwError}</div>
              )}
              {pwSuccess && (
                <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-800 text-sm">Contraseña actualizada correctamente.</div>
              )}

              <form onSubmit={handlePasswordSave} className="space-y-4">
                {(['current', 'next', 'confirm'] as const).map((field) => {
                  const labels = { current: 'Contraseña actual', next: 'Nueva contraseña', confirm: 'Confirmar nueva contraseña' };
                  const errors = { current: pwErrors.current, next: pwErrors.next, confirm: pwErrors.confirm };
                  return (
                    <div key={field}>
                      <label className="block text-xs font-medium text-gray-700 mb-1">
                        {labels[field]} <span className="text-red-500">*</span>
                      </label>
                      <div className="relative">
                        <input
                          type={showPw[field] ? 'text' : 'password'}
                          value={pwForm[field]}
                          onChange={e => { setPwForm(f => ({ ...f, [field]: e.target.value })); setPwErrors(er => ({ ...er, [field]: '' })); }}
                          className={`${inputCls(errors[field])} pr-10`}
                        />
                        <button type="button" onClick={() => setShowPw(s => ({ ...s, [field]: !s[field] }))}
                          className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600">
                          {showPw[field] ? (
                            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                            </svg>
                          ) : (
                            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                            </svg>
                          )}
                        </button>
                      </div>
                      {errors[field] && <p className="mt-1 text-xs text-red-600">{errors[field]}</p>}
                    </div>
                  );
                })}

                <p className="text-xs text-gray-400">La contraseña debe tener al menos 8 caracteres.</p>

                <div className="pt-2">
                  <button type="submit" disabled={pwSaving}
                    className="w-full py-2.5 bg-medin-navy text-white font-semibold rounded-lg hover:bg-medin-blue transition-colors disabled:opacity-50 text-sm">
                    {pwSaving ? 'Actualizando...' : 'Cambiar contraseña'}
                  </button>
                </div>
              </form>
            </div>

          </div>
        )}
      </main>
    </div>
  );
};

export default PatientProfilePage;
