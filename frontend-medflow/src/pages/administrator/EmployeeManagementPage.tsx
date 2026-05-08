import { useState, useEffect } from 'react';
import type { FC, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  listEmployees,
  toggleEmployeeActive,
  createEmployee,
  getEmployee,
  updateEmployee,
  EMPLOYEE_ROLES,
} from '../../services/employeeService';
import type { Employee, CreateEmployeeData } from '../../services/employeeService';

type ViewMode = 'list' | 'create' | 'edit';

const INITIAL_FORM: CreateEmployeeData = {
  firstName: '',
  secondName: '',
  firstLastName: '',
  secondLastName: '',
  email: '',
  phone: '',
  roleName: '',
};

const EmployeeManagementPage: FC = () => {
  const navigate = useNavigate();
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [selectedId, setSelectedId] = useState<string | null>(null);

  // ── Lista ─────────────────────────────────────────────────────────────────
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [listLoading, setListLoading] = useState(true);
  const [listError, setListError] = useState('');
  const [rolFilter, setRolFilter] = useState('');
  const [activoFilter, setActivoFilter] = useState('');
  const [togglingId, setTogglingId] = useState<string | null>(null);

  const fetchEmployees = async () => {
    setListLoading(true);
    setListError('');
    try {
      const activo = activoFilter === '' ? undefined : activoFilter === 'true';
      const data = await listEmployees(rolFilter || undefined, activo);
      setEmployees(data);
    } catch {
      setListError('No se pudo cargar la lista de empleados.');
    } finally {
      setListLoading(false);
    }
  };

  useEffect(() => {
    if (viewMode === 'list') fetchEmployees();
  }, [rolFilter, activoFilter, viewMode]);

  const handleToggle = async (id: string) => {
    setTogglingId(id);
    try {
      const updated = await toggleEmployeeActive(id);
      setEmployees(prev => prev.map(e => e.id === id ? updated : e));
    } catch {
      setListError('No se pudo cambiar el estado del empleado.');
    } finally {
      setTogglingId(null);
    }
  };

  // ── Formulario ────────────────────────────────────────────────────────────
  const [form, setForm] = useState<CreateEmployeeData>(INITIAL_FORM);
  const [formLoading, setFormLoading] = useState(false);
  const [formLoadingData, setFormLoadingData] = useState(false);
  const [formError, setFormError] = useState('');
  const [tempPassword, setTempPassword] = useState('');

  const openCreate = () => {
    setForm(INITIAL_FORM);
    setFormError('');
    setTempPassword('');
    setSelectedId(null);
    setViewMode('create');
  };

  const openEdit = (emp: Employee) => {
    setForm({
      firstName: emp.firstName,
      secondName: emp.secondName ?? '',
      firstLastName: emp.firstLastName,
      secondLastName: emp.secondLastName ?? '',
      email: emp.email,
      phone: emp.phone ?? '',
      roleName: emp.role,
    });
    setFormError('');
    setTempPassword('');
    setSelectedId(emp.id);
    setViewMode('edit');
  };

  useEffect(() => {
    if (viewMode === 'edit' && selectedId) {
      setFormLoadingData(true);
      getEmployee(selectedId)
        .then(emp => {
          setForm({
            firstName: emp.firstName,
            secondName: emp.secondName ?? '',
            firstLastName: emp.firstLastName,
            secondLastName: emp.secondLastName ?? '',
            email: emp.email,
            phone: emp.phone ?? '',
            roleName: emp.role,
          });
        })
        .catch(() => setFormError('No se pudo cargar el empleado.'))
        .finally(() => setFormLoadingData(false));
    }
  }, [viewMode, selectedId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const validate = (): string => {
    if (!form.firstName.trim()) return 'El primer nombre es requerido.';
    if (!form.firstLastName.trim()) return 'El primer apellido es requerido.';
    if (!form.email.trim()) return 'El correo electrónico es requerido.';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Formato de correo inválido.';
    if (form.phone && !/^\d{8}$/.test(form.phone)) return 'El teléfono debe tener 8 dígitos.';
    if (!form.roleName) return 'Debe seleccionar un rol.';
    return '';
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const validationError = validate();
    if (validationError) { setFormError(validationError); return; }

    setFormError('');
    setFormLoading(true);
    try {
      const payload: CreateEmployeeData = {
        firstName: form.firstName.trim(),
        secondName: form.secondName?.trim() || undefined,
        firstLastName: form.firstLastName.trim(),
        secondLastName: form.secondLastName?.trim() || undefined,
        email: form.email.trim(),
        phone: form.phone?.trim() || undefined,
        roleName: form.roleName,
      };

      if (viewMode === 'edit' && selectedId) {
        await updateEmployee(selectedId, payload);
        setViewMode('list');
      } else {
        const result = await createEmployee(payload);
        setTempPassword(result.contrasenaTemporalParaEntregar);
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setFormError(axiosErr.response?.data?.message ?? 'Ocurrió un error. Intente de nuevo.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleCancel = () => {
    setViewMode('list');
    setSelectedId(null);
    setTempPassword('');
    setFormError('');
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => viewMode === 'list' ? navigate('/administrator') : handleCancel()}
              className="text-gray-400 hover:text-gray-600"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Gestión de Personal</h2>
              <p className="text-gray-500 text-sm">Registrar, editar y administrar cuentas del personal</p>
            </div>
          </div>
          {viewMode === 'list' && (
            <button
              onClick={openCreate}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
            >
              + Nuevo Empleado
            </button>
          )}
        </div>

        {/* Lista */}
        {viewMode === 'list' && (
          <>
            <div className="bg-white p-4 rounded-lg shadow flex flex-wrap gap-4">
              <div>
                <label className="block text-xs text-gray-500 mb-1">Rol</label>
                <select
                  value={rolFilter}
                  onChange={e => setRolFilter(e.target.value)}
                  className="border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Todos los roles</option>
                  {Object.entries(EMPLOYEE_ROLES).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Estado</label>
                <select
                  value={activoFilter}
                  onChange={e => setActivoFilter(e.target.value)}
                  className="border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Todos</option>
                  <option value="true">Activos</option>
                  <option value="false">Inactivos</option>
                </select>
              </div>
            </div>

            {listError && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                {listError}
              </div>
            )}

            <div className="bg-white rounded-lg shadow overflow-hidden">
              {listLoading ? (
                <div className="flex items-center justify-center py-16">
                  <div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent" />
                </div>
              ) : employees.length === 0 ? (
                <div className="text-center py-16 text-gray-500">
                  No se encontraron empleados con los filtros seleccionados.
                </div>
              ) : (
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Usuario</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Correo</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Rol</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {employees.map(emp => (
                      <tr key={emp.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="font-medium text-gray-900">{emp.fullName}</div>
                          {emp.phone && <div className="text-xs text-gray-500">{emp.phone}</div>}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{emp.username}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{emp.email}</td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded-full">
                            {EMPLOYEE_ROLES[emp.role] ?? emp.role}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                            emp.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                          }`}>
                            {emp.active ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right space-x-2">
                          <button
                            onClick={() => openEdit(emp)}
                            className="text-sm text-blue-600 hover:text-blue-800 font-medium"
                          >
                            Editar
                          </button>
                          <button
                            onClick={() => handleToggle(emp.id)}
                            disabled={togglingId === emp.id}
                            className={`text-sm font-medium ${
                              emp.active ? 'text-red-600 hover:text-red-800' : 'text-green-600 hover:text-green-800'
                            } disabled:opacity-50`}
                          >
                            {togglingId === emp.id ? '...' : emp.active ? 'Desactivar' : 'Activar'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </>
        )}

        {/* Contraseña temporal post-creación */}
        {(viewMode === 'create' || viewMode === 'edit') && tempPassword && (
          <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8 text-center space-y-4">
            <div className="h-16 w-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
              <svg className="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-gray-900">Empleado registrado</h3>
            <p className="text-gray-600">La cuenta fue creada exitosamente. Entregue esta contraseña temporal al empleado:</p>
            <div className="bg-yellow-50 border border-yellow-300 rounded-lg p-4">
              <p className="text-xs text-yellow-700 mb-1 font-medium">CONTRASEÑA TEMPORAL</p>
              <p className="text-2xl font-mono font-bold text-yellow-900 tracking-widest">{tempPassword}</p>
            </div>
            <p className="text-xs text-gray-500">Esta contraseña solo se muestra una vez. Anótela antes de continuar.</p>
            <div className="flex gap-3 justify-center pt-2">
              <button
                onClick={() => { setTempPassword(''); setForm(INITIAL_FORM); }}
                className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
              >
                Registrar otro
              </button>
              <button
                onClick={handleCancel}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Ver lista
              </button>
            </div>
          </div>
        )}

        {/* Formulario crear/editar */}
        {(viewMode === 'create' || viewMode === 'edit') && !tempPassword && (
          <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-5">
              {viewMode === 'edit' ? 'Editar Empleado' : 'Nuevo Empleado'}
            </h3>

            {formLoadingData ? (
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-4 border-blue-500 border-t-transparent" />
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-5">
                {formError && (
                  <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                    {formError}
                  </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Primer Nombre <span className="text-red-500">*</span>
                    </label>
                    <input type="text" name="firstName" value={form.firstName} onChange={handleChange}
                      placeholder="Ej: Juan"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Segundo Nombre</label>
                    <input type="text" name="secondName" value={form.secondName} onChange={handleChange}
                      placeholder="Opcional"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Primer Apellido <span className="text-red-500">*</span>
                    </label>
                    <input type="text" name="firstLastName" value={form.firstLastName} onChange={handleChange}
                      placeholder="Ej: Pérez"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Segundo Apellido</label>
                    <input type="text" name="secondLastName" value={form.secondLastName} onChange={handleChange}
                      placeholder="Opcional"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Correo Electrónico <span className="text-red-500">*</span>
                    </label>
                    <input type="email" name="email" value={form.email} onChange={handleChange}
                      placeholder="correo@hospital.com"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                    <input type="text" name="phone" value={form.phone} onChange={handleChange}
                      placeholder="12345678" maxLength={8}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Rol <span className="text-red-500">*</span>
                  </label>
                  <select name="roleName" value={form.roleName} onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">Seleccionar rol...</option>
                    {Object.entries(EMPLOYEE_ROLES).map(([key, label]) => (
                      <option key={key} value={key}>{label}</option>
                    ))}
                  </select>
                </div>

                <div className="flex gap-3 pt-2">
                  <button type="button" onClick={handleCancel}
                    className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium">
                    Cancelar
                  </button>
                  <button type="submit" disabled={formLoading}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50">
                    {formLoading ? 'Guardando...' : viewMode === 'edit' ? 'Guardar Cambios' : 'Registrar Empleado'}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default EmployeeManagementPage;
