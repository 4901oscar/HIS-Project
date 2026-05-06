import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { getClinics, deleteClinic } from '../../services/clinicService';
import type { Clinic, ClinicStatus } from '../../types/clinic';
import { useAuth } from '../../hooks/useAuth';

interface ClinicListProps {
  onCreateClick?: () => void;
  onEditClick?: (clinic: Clinic) => void;
}

const ClinicList: FC<ClinicListProps> = ({ onCreateClick, onEditClick }) => {
  const { user } = useAuth();
  const isAdmin = user?.roles?.includes('ADMIN');

  const [clinics, setClinics] = useState<Clinic[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<ClinicStatus | ''>('');

  useEffect(() => {
    loadClinics();
  }, [statusFilter]);

  const loadClinics = async () => {
    try {
      setIsLoading(true);
      const data = await getClinics(statusFilter || undefined);
      setClinics(data);
      setError(null);
    } catch (err) {
      setError('Error al cargar la lista de clínicas');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async (clinic: Clinic) => {
    if (!window.confirm(`¿Estás seguro de eliminar la clínica "${clinic.nombre}"?`)) {
      return;
    }

    try {
      await deleteClinic(clinic.id);
      await loadClinics(); // Reload list
    } catch (err) {
      alert('Error al eliminar la clínica');
      console.error(err);
    }
  };

  const filteredClinics = clinics.filter(clinic =>
    clinic.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    clinic.codigo.toLowerCase().includes(searchTerm.toLowerCase()) ||
    clinic.descripcion.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getStatusBadge = (estado: ClinicStatus) => {
    const badges = {
      ACTIVE: 'bg-green-100 text-green-800',
      INACTIVE: 'bg-yellow-100 text-yellow-800',
      DELETED: 'bg-red-100 text-red-800',
    };
    const labels = {
      ACTIVE: 'Activa',
      INACTIVE: 'Inactiva',
      DELETED: 'Eliminada',
    };
    return (
      <span className={`inline-block px-2 py-1 text-xs font-semibold rounded ${badges[estado]}`}>
        {labels[estado]}
      </span>
    );
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="text-gray-600">Cargando clínicas...</div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold text-gray-800">Lista de Clínicas</h3>
        {isAdmin && (
          <button
            onClick={onCreateClick}
            className="px-4 py-2 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors"
          >
            + Crear Clínica
          </button>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Filters */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <input
            type="text"
            placeholder="Buscar por código, nombre o descripción..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-medin-cyan"
          />
        </div>
        <div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as ClinicStatus | '')}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-medin-cyan"
          >
            <option value="">Todos los estados</option>
            <option value="ACTIVE">Activas</option>
            <option value="INACTIVE">Inactivas</option>
            <option value="DELETED">Eliminadas</option>
          </select>
        </div>
      </div>

      {/* Clinic Table */}
      {!error && filteredClinics.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          {searchTerm || statusFilter ? 'No se encontraron clínicas' : 'No hay clínicas registradas'}
        </div>
      ) : !error ? (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white border border-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Código
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nombre
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Descripción
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                {isAdmin && (
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Acciones
                  </th>
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredClinics.map((clinic) => (
                <tr key={clinic.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {clinic.codigo}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {clinic.nombre}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {clinic.descripcion}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    {getStatusBadge(clinic.estado)}
                  </td>
                  {isAdmin && (
                    <td className="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                      <button
                        onClick={() => onEditClick?.(clinic)}
                        className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => handleDelete(clinic)}
                        className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
                      >
                        Eliminar
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  );
};

export default ClinicList;
