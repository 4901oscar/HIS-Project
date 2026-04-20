import { useState, useEffect } from 'react';
import type { FC, FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  createEmployee,
  getEmployee,
  updateEmployee,
  EMPLOYEE_ROLES,
} from '../../services/employeeService';
import type { CreateEmployeeData } from '../../services/employeeService';

const INITIAL_FORM: CreateEmployeeData = {
  firstName: '',
  secondName: '',
  firstLastName: '',
  secondLastName: '',
  email: '',
  phone: '',
  roleName: '',
};

const EmployeeFormPage: FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);

  const [form, setForm] = useState<CreateEmployeeData>(INITIAL_FORM);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(isEditing);
  const [error, setError] = useState('');
  const [tempPassword, setTempPassword] = useState('');

  useEffect(() => {
    if (!isEditing || !id) return;
    getEmployee(id)
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
      .catch(() => setError('No se pudo cargar el empleado.'))
      .finally(() => setLoadingData(false));
  }, [id, isEditing]);

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
    if (validationError) { setError(validationError); return; }

    setError('');
    setLoading(true);
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

      if (isEditing && id) {
        await updateEmployee(id, payload);
        navigate('/administrator/empleados');
      } else {
        const result = await createEmployee(payload);
        setTempPassword(result.contrasenaTemporalParaEntregar);
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message ?? 'Ocurrió un error. Intente de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  if (loadingData) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center py-24">
          <div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-500 border-t-transparent" />
        </div>
      </MainLayout>
    );
  }

  // Éxito al crear — mostrar contraseña temporal
  if (tempPassword) {
    return (
      <MainLayout>
        <div className="max-w-lg mx-auto mt-12">
          <div className="bg-white rounded-lg shadow p-8 text-center space-y-4">
            <div className="h-16 w-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
              <svg className="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-gray-900">Empleado registrado</h3>
            <p className="text-gray-600">
              La cuenta fue creada exitosamente. Entregue esta contraseña temporal al empleado:
            </p>
            <div className="bg-yellow-50 border border-yellow-300 rounded-lg p-4">
              <p className="text-xs text-yellow-700 mb-1 font-medium">CONTRASEÑA TEMPORAL</p>
              <p className="text-2xl font-mono font-bold text-yellow-900 tracking-widest">{tempPassword}</p>
            </div>
            <p className="text-xs text-gray-500">
              Esta contraseña solo se muestra una vez. Anótela antes de continuar.
            </p>
            <div className="flex gap-3 justify-center pt-2">
              <button
                onClick={() => { setTempPassword(''); setForm(INITIAL_FORM); }}
                className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
              >
                Registrar otro
              </button>
              <button
                onClick={() => navigate('/administrator/empleados')}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Ver lista
              </button>
            </div>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Encabezado */}
        <div>
          <button
            onClick={() => navigate('/administrator/empleados')}
            className="text-sm text-gray-500 hover:text-gray-700 mb-1 flex items-center gap-1"
          >
            ← Gestión de Personal
          </button>
          <h2 className="text-2xl font-bold text-gray-900">
            {isEditing ? 'Editar Empleado' : 'Nuevo Empleado'}
          </h2>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-5">

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* Nombres */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Primer Nombre <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="firstName"
                value={form.firstName}
                onChange={handleChange}
                placeholder="Ej: Juan"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Segundo Nombre
              </label>
              <input
                type="text"
                name="secondName"
                value={form.secondName}
                onChange={handleChange}
                placeholder="Opcional"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Apellidos */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Primer Apellido <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="firstLastName"
                value={form.firstLastName}
                onChange={handleChange}
                placeholder="Ej: Pérez"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Segundo Apellido
              </label>
              <input
                type="text"
                name="secondLastName"
                value={form.secondLastName}
                onChange={handleChange}
                placeholder="Opcional"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Correo y Teléfono */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Correo Electrónico <span className="text-red-500">*</span>
              </label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="correo@hospital.com"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Teléfono
              </label>
              <input
                type="text"
                name="phone"
                value={form.phone}
                onChange={handleChange}
                placeholder="12345678"
                maxLength={8}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Rol */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Rol <span className="text-red-500">*</span>
            </label>
            <select
              name="roleName"
              value={form.roleName}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Seleccionar rol...</option>
              {Object.entries(EMPLOYEE_ROLES).map(([key, label]) => (
                <option key={key} value={key}>{label}</option>
              ))}
            </select>
          </div>

          {/* Acciones */}
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={() => navigate('/administrator/empleados')}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50"
            >
              {loading ? 'Guardando...' : isEditing ? 'Guardar Cambios' : 'Registrar Empleado'}
            </button>
          </div>
        </form>
      </div>
    </MainLayout>
  );
};

export default EmployeeFormPage;
