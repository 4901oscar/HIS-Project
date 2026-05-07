import { useState, useEffect } from 'react';
import type { FC } from 'react';
import Swal from 'sweetalert2';
import { listActiveDoctors, deactivateDoctor, type Doctor } from '../../services/doctorService';
import { getClinics } from '../../services/clinicService';
import type { Clinic } from '../../types/clinic';

interface DoctorListProps {
  onCreateClick?: () => void;
  onEditClick?: (doctor: Doctor) => void;
  onManageDaysOff?: (doctor: Doctor) => void;
}

const DoctorList: FC<DoctorListProps> = ({ onCreateClick, onEditClick, onManageDaysOff }) => {
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [clinicsMap, setClinicsMap] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [doctorsData, clinicsData] = await Promise.all([
        listActiveDoctors(),
        getClinics('ACTIVE'),
      ]);
      setDoctors(doctorsData);
      const map: Record<string, string> = {};
      clinicsData.forEach((c: Clinic) => { map[c.id] = c.nombre; });
      setClinicsMap(map);
      setError(null);
    } catch {
      setError('Error al cargar la lista de doctores');
    } finally {
      setIsLoading(false);
    }
  };

  const loadDoctors = loadData;

  const handleDeactivate = async (doctor: Doctor) => {
    const result = await Swal.fire({
      title: '¿Desactivar doctor?',
      text: `Dr. ${doctor.name} no podrá recibir citas mientras esté inactivo.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, desactivar',
      cancelButtonText: 'Cancelar',
    });
    if (!result.isConfirmed) return;
    try {
      await deactivateDoctor(doctor.id);
      await loadDoctors();
    } catch {
      Swal.fire({ title: 'Error', text: 'No se pudo desactivar el doctor. Intenta de nuevo.', icon: 'error' });
    }
  };

  const filteredDoctors = doctors.filter(doctor =>
    doctor.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const formatShift = (start: string, end: string) => {
    return `${start} - ${end}`;
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="text-gray-600">Cargando doctores...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800">Gestión de Doctores</h2>
        <button
          onClick={onCreateClick}
          className="px-4 py-2 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors"
        >
          + Vincular Doctor
        </button>
      </div>

      {/* Search */}
      <div>
        <input
          type="text"
          placeholder="Buscar por nombre o especialidad..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-medin-cyan"
        />
      </div>

      {/* Doctor List */}
      {filteredDoctors.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          {searchTerm ? 'No se encontraron doctores' : 'No hay doctores registrados'}
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredDoctors.map((doctor) => (
            <div
              key={doctor.id}
              className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow"
            >
              <div className="space-y-2">
                <h3 className="text-lg font-semibold text-gray-800">
                  Dr. {doctor.name}
                </h3>
                <p className="text-sm text-gray-500">
                  Clínica: {doctor.clinicId ? (clinicsMap[doctor.clinicId] ?? doctor.clinicId) : 'Sin asignar'}
                </p>
                <p className="text-sm text-gray-500">
                  Turno: {formatShift(doctor.shiftStart, doctor.shiftEnd)}
                </p>
                <div className="flex items-center space-x-2">
                  <span
                    className={`inline-block px-2 py-1 text-xs font-semibold rounded ${
                      doctor.status === 'ACTIVE'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {doctor.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}
                  </span>
                </div>
              </div>

              {/* Actions */}
              <div className="mt-4 flex space-x-2">
                <button
                  onClick={() => onEditClick?.(doctor)}
                  className="flex-1 px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                >
                  Editar
                </button>
                <button
                  onClick={() => onManageDaysOff?.(doctor)}
                  className="flex-1 px-3 py-1 text-sm bg-yellow-500 text-white rounded hover:bg-yellow-600 transition-colors"
                >
                  Días Libres
                </button>
                <button
                  onClick={() => handleDeactivate(doctor)}
                  className="flex-1 px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
                >
                  Desactivar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default DoctorList;
