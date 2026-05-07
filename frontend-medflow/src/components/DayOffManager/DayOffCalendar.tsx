import { useState, useEffect } from 'react';
import type { FC } from 'react';
import Swal from 'sweetalert2';
import { getDoctorDaysOff, removeDayOff, type Doctor, type DayOff } from '../../services/doctorService';

interface DayOffCalendarProps {
  doctor: Doctor;
  onDayOffRemoved?: () => void;
}

interface CalendarDay {
  date: Date;
  dateString: string;
  isCurrentMonth: boolean;
  isToday: boolean;
  isDayOff: boolean;
  dayOffReason?: string;
}

const DayOffCalendar: FC<DayOffCalendarProps> = ({ doctor, onDayOffRemoved }) => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [daysOff, setDaysOff] = useState<DayOff[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedDay, setSelectedDay] = useState<CalendarDay | null>(null);

  const monthNames = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  const dayNames = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

  useEffect(() => {
    loadDaysOff();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [doctor.id, currentDate]);

  const loadDaysOff = async () => {
    setIsLoading(true);
    try {
      const data = await getDoctorDaysOff(doctor.id);
      setDaysOff(data);
    } catch {
      // Days off load failure is non-blocking — calendar shows empty
    } finally {
      setIsLoading(false);
    }
  };

  const generateCalendarDays = (): CalendarDay[] => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startingDayOfWeek = firstDay.getDay();
    const daysInMonth = lastDay.getDate();

    const days: CalendarDay[] = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Previous month days
    const prevMonthLastDay = new Date(year, month, 0).getDate();
    for (let i = startingDayOfWeek - 1; i >= 0; i--) {
      const date = new Date(year, month - 1, prevMonthLastDay - i);
      const dateString = date.toISOString().split('T')[0];
      const dayOff = daysOff.find(d => d.date === dateString);
      
      days.push({
        date,
        dateString,
        isCurrentMonth: false,
        isToday: false,
        isDayOff: !!dayOff,
        dayOffReason: dayOff?.reason,
      });
    }

    // Current month days
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day);
      const dateString = date.toISOString().split('T')[0];
      const dayOff = daysOff.find(d => d.date === dateString);
      
      days.push({
        date,
        dateString,
        isCurrentMonth: true,
        isToday: date.getTime() === today.getTime(),
        isDayOff: !!dayOff,
        dayOffReason: dayOff?.reason,
      });
    }

    // Next month days
    const remainingDays = 42 - days.length; // 6 weeks * 7 days
    for (let day = 1; day <= remainingDays; day++) {
      const date = new Date(year, month + 1, day);
      const dateString = date.toISOString().split('T')[0];
      const dayOff = daysOff.find(d => d.date === dateString);
      
      days.push({
        date,
        dateString,
        isCurrentMonth: false,
        isToday: false,
        isDayOff: !!dayOff,
        dayOffReason: dayOff?.reason,
      });
    }

    return days;
  };

  const handlePreviousMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
  };

  const handleNextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
  };

  const handleDayClick = (day: CalendarDay) => {
    if (day.isDayOff) {
      setSelectedDay(day);
    }
  };

  const handleRemoveDayOff = async () => {
    if (!selectedDay) return;

    const result = await Swal.fire({
      title: '¿Eliminar día libre?',
      text: selectedDay.date.toLocaleDateString('es-GT', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }),
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
    });

    if (!result.isConfirmed) return;

    try {
      await removeDayOff(doctor.id, selectedDay.dateString);
      setSelectedDay(null);
      await loadDaysOff();
      onDayOffRemoved?.();
      Swal.fire({ title: 'Eliminado', text: 'Día libre eliminado exitosamente', icon: 'success', timer: 2000, showConfirmButton: false });
    } catch (err: unknown) {
      const errorMessage =
        err instanceof Error ? err.message : 'Error al eliminar día libre';
      Swal.fire({ title: 'Error', text: errorMessage, icon: 'error' });
    }
  };

  const calendarDays = generateCalendarDays();

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="bg-blue-50 border border-blue-200 p-4 rounded">
        <h2 className="text-xl font-bold text-gray-800">
          Calendario de Disponibilidad
        </h2>
        <p className="text-sm text-gray-600 mt-1">
          Dr. {doctor.name}
        </p>
        <p className="text-xs text-gray-500 mt-1">
          Turno: {doctor.shiftStart} - {doctor.shiftEnd}
        </p>
      </div>

      {/* Calendar Navigation */}
      <div className="flex items-center justify-between bg-white p-4 border border-gray-200 rounded">
        <button
          onClick={handlePreviousMonth}
          className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded transition-colors"
        >
          ← Anterior
        </button>
        <h3 className="text-lg font-semibold text-gray-800">
          {monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}
        </h3>
        <button
          onClick={handleNextMonth}
          className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded transition-colors"
        >
          Siguiente →
        </button>
      </div>

      {/* Calendar Grid */}
      <div className="bg-white border border-gray-200 rounded overflow-hidden">
        {/* Day Names */}
        <div className="grid grid-cols-7 bg-gray-100 border-b border-gray-200">
          {dayNames.map((day) => (
            <div
              key={day}
              className="text-center py-2 text-sm font-semibold text-gray-700"
            >
              {day}
            </div>
          ))}
        </div>

        {/* Calendar Days */}
        <div className="grid grid-cols-7">
          {isLoading ? (
            <div className="col-span-7 text-center py-12 text-gray-500">
              Cargando calendario...
            </div>
          ) : (
            calendarDays.map((day, index) => (
              <div
                key={index}
                onClick={() => handleDayClick(day)}
                className={`
                  min-h-[80px] p-2 border-b border-r border-gray-200
                  ${!day.isCurrentMonth ? 'bg-gray-50' : 'bg-white'}
                  ${day.isToday ? 'ring-2 ring-medin-cyan' : ''}
                  ${day.isDayOff ? 'bg-red-100 cursor-pointer hover:bg-red-200' : 'bg-green-50'}
                  ${day.isDayOff && !day.isCurrentMonth ? 'bg-red-50' : ''}
                  transition-colors
                `}
              >
                <div className="flex flex-col h-full">
                  <span
                    className={`
                      text-sm font-medium
                      ${!day.isCurrentMonth ? 'text-gray-400' : 'text-gray-700'}
                      ${day.isToday ? 'text-medin-cyan font-bold' : ''}
                    `}
                  >
                    {day.date.getDate()}
                  </span>
                  {day.isDayOff && (
                    <div className="mt-1 flex-1">
                      <div className="text-xs text-red-700 font-semibold">
                        Día Libre
                      </div>
                      {day.dayOffReason && (
                        <div className="text-xs text-red-600 mt-1 line-clamp-2">
                          {day.dayOffReason}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Legend */}
      <div className="flex items-center justify-center space-x-6 text-sm">
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 bg-green-50 border border-gray-300 rounded"></div>
          <span className="text-gray-700">Disponible</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 bg-red-100 border border-gray-300 rounded"></div>
          <span className="text-gray-700">Día Libre</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-4 h-4 border-2 border-medin-cyan rounded"></div>
          <span className="text-gray-700">Hoy</span>
        </div>
      </div>

      {/* Selected Day Details */}
      {selectedDay && (
        <div className="bg-yellow-50 border border-yellow-200 p-4 rounded">
          <h4 className="font-semibold text-gray-800 mb-2">
            Día Libre Seleccionado
          </h4>
          <p className="text-sm text-gray-700 mb-1">
            <strong>Fecha:</strong> {selectedDay.date.toLocaleDateString('es-ES', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            })}
          </p>
          {selectedDay.dayOffReason && (
            <p className="text-sm text-gray-700 mb-3">
              <strong>Motivo:</strong> {selectedDay.dayOffReason}
            </p>
          )}
          <div className="flex space-x-3">
            <button
              onClick={handleRemoveDayOff}
              className="px-4 py-2 bg-red-600 text-white font-semibold hover:bg-red-700 transition-colors rounded"
            >
              ELIMINAR DÍA LIBRE
            </button>
            <button
              onClick={() => setSelectedDay(null)}
              className="px-4 py-2 bg-gray-300 text-gray-700 font-semibold hover:bg-gray-400 transition-colors rounded"
            >
              CANCELAR
            </button>
          </div>
        </div>
      )}

      {/* Statistics */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-green-50 border border-green-200 p-4 rounded text-center">
          <div className="text-2xl font-bold text-green-700">
            {calendarDays.filter(d => d.isCurrentMonth && !d.isDayOff).length}
          </div>
          <div className="text-sm text-gray-600 mt-1">Días Disponibles</div>
        </div>
        <div className="bg-red-50 border border-red-200 p-4 rounded text-center">
          <div className="text-2xl font-bold text-red-700">
            {calendarDays.filter(d => d.isCurrentMonth && d.isDayOff).length}
          </div>
          <div className="text-sm text-gray-600 mt-1">Días Libres</div>
        </div>
      </div>
    </div>
  );
};

export default DayOffCalendar;
