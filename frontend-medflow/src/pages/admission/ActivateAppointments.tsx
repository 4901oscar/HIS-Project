/**
 * ActivateAppointments - Vista de Admisión para Activar Citas
 */

import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import { 
  MagnifyingGlassIcon, 
  CheckCircleIcon,
  ClockIcon,
  XCircleIcon,
} from '@heroicons/react/24/outline';
import { getAppointments, activateAppointment } from '../../services/appointmentService';

interface Appointment {
  id: string;
  patientName: string;
  patientDPI: string;
  date: string;
  time: string;
  status: 'PENDING' | 'ACTIVATED' | 'CANCELLED';
  doctor?: string;
  specialty?: string;
}

const ActivateAppointments: FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [filteredAppointments, setFilteredAppointments] = useState<Appointment[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    loadAppointments();
  }, []);

  useEffect(() => {
    // Filtrar citas por búsqueda
    if (searchTerm.trim() === '') {
      setFilteredAppointments(appointments);
    } else {
      const filtered = appointments.filter(
        (apt) =>
          apt.patientName.toLowerCase().includes(searchTerm.toLowerCase()) ||
          apt.patientDPI.includes(searchTerm) ||
          apt.id.includes(searchTerm)
      );
      setFilteredAppointments(filtered);
    }
  }, [searchTerm, appointments]);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      const data = await getAppointments();
      setAppointments(data as Appointment[]);
      setFilteredAppointments(data as Appointment[]);
    } catch {
      setErrorMessage('Error loading appointments');
    } finally {
      setLoading(false);
    }
  };

  const handleActivate = async (appointmentId: string) => {
    try {
      const result = await activateAppointment(appointmentId) as { status: number; data: { message: string } };
      
      if (result.status === 200) {
        // Actualizar lista
        setAppointments((prev) =>
          prev.map((apt) =>
            apt.id === appointmentId ? { ...apt, status: 'ACTIVATED' as const } : apt
          )
        );
        
        setSuccessMessage(`Appointment ${appointmentId} activated successfully`);
        setTimeout(() => setSuccessMessage(null), 3000);
      }
    } catch {
      setErrorMessage('Error activating appointment');
      setTimeout(() => setErrorMessage(null), 3000);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <ClockIcon className="h-4 w-4 mr-1" />
            Pending
          </span>
        );
      case 'ACTIVATED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircleIcon className="h-4 w-4 mr-1" />
            Activated
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <XCircleIcon className="h-4 w-4 mr-1" />
            Cancelled
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Activate Appointments</h2>
            <p className="mt-1 text-sm text-gray-600">
              Review and activate pending patient appointments
            </p>
          </div>
          <button
            onClick={loadAppointments}
            className="mt-4 sm:mt-0 px-4 py-2 bg-medin-navy text-white rounded-lg hover:bg-medin-navy/90 transition-colors"
          >
            Refresh
          </button>
        </div>

          {/* Messages */}
        {successMessage && (
          <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg">
            {successMessage}
          </div>
        )}
        {errorMessage && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg">
            {errorMessage}
          </div>
        )}

        {/* Search Bar */}
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="relative">
            <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search by patient name, DPI, or appointment ID..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent"
            />
          </div>
        </div>

        {/* Appointments Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {loading ? (
            <div className="p-12 text-center">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
              <p className="mt-4 text-gray-600">Loading appointments...</p>
            </div>
          ) : filteredAppointments.length === 0 ? (
            <div className="p-12 text-center text-gray-500">
              No appointments found
            </div>
          ) : (
            <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Appointment ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Patient
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    DPI
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date & Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Doctor / Specialty
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredAppointments.map((appointment) => (
                  <tr key={appointment.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {appointment.id}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {appointment.patientName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {appointment.patientDPI}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      <div>{appointment.date}</div>
                      <div className="text-xs text-gray-500">{appointment.time}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      <div>{appointment.doctor || 'Not assigned'}</div>
                      <div className="text-xs text-gray-500">{appointment.specialty || '-'}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(appointment.status)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {appointment.status === 'PENDING' && (
                        <button
                          onClick={() => handleActivate(appointment.id)}
                          className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-medin-cyan hover:bg-medin-cyan/90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-medin-cyan"
                        >
                          <CheckCircleIcon className="h-4 w-4 mr-1" />
                          Activate
                        </button>
                      )}
                      {appointment.status === 'ACTIVATED' && (
                        <span className="text-green-600 text-xs">✓ Active</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
};

export default ActivateAppointments;
