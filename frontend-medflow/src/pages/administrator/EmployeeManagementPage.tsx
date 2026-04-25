import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  listEmployees,
  toggleEmployeeActive,
  EMPLOYEE_ROLES,
} from '../../services/employeeService';
import type { Employee } from '../../services/employeeService';

const EmployeeManagementPage: FC = () => {
  const navigate = useNavigate();
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [rolFilter, setRolFilter] = useState('');
  const [activoFilter, setActivoFilter] = useState<string>('');
  const [togglingId, setTogglingId] = useState<string | null>(null);

  const fetchEmployees = async () => {
    setLoading(true);
    setError('');
    try {
      const activo = activoFilter === '' ? undefined : activoFilter === 'true';
      const data = await listEmployees(rolFilter || undefined, activo);
      setEmployees(data);
    } catch {
      setError('No se pudo cargar la lista de empleados.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, [rolFilter, activoFilter]);

  const handleToggle = async (id: string) => {
    setTogglingId(id);
    try {
      const updated = await toggleEmployeeActive(id);
      setEmployees(prev => prev.map(e => e.id === id ? updated : e));
    } catch {
      setError('No se pudo cambiar el estado del empleado.');
    } finally {
      setTogglingId(null);
    }
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Encabezado */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/administrator')}
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
          <button
            onClick={() => navigate('/administrator/empleados/nuevo')}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
          >
            + Nuevo Empleado
          </button>
        </div>

        {/* Filtros */}
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

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Tabla */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {loading ? (
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
                        emp.active
                          ? 'bg-green-100 text-green-800'
                          : 'bg-red-100 text-red-800'
                      }`}>
                        {emp.active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right space-x-2">
                      <button
                        onClick={() => navigate(`/administrator/empleados/${emp.id}/editar`)}
                        className="text-sm text-blue-600 hover:text-blue-800 font-medium"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => handleToggle(emp.id)}
                        disabled={togglingId === emp.id}
                        className={`text-sm font-medium ${
                          emp.active
                            ? 'text-red-600 hover:text-red-800'
                            : 'text-green-600 hover:text-green-800'
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
      </div>
    </MainLayout>
  );
};

export default EmployeeManagementPage;
