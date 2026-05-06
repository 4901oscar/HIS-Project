/**
 * ScheduleHours - Panel de horarios de atención
 */

import type { FC } from 'react';

const ScheduleHours: FC = () => {
  const schedule = [
    { day: 'Lunes', hours: '09:00 AM - 07:00 PM' },
    { day: 'Martes', hours: '09:00 AM - 07:00 PM' },
    { day: 'Miércoles', hours: '09:00 AM - 07:00 PM' },
    { day: 'Jueves', hours: '09:00 AM - 07:00 PM' },
    { day: 'Viernes', hours: '09:00 AM - 07:00 PM' },
    { day: 'Sábado', hours: '09:00 AM - 07:00 PM' },
    { day: 'Domingo', hours: 'Cerrado' },
  ];

  return (
    <div className="bg-medin-navy text-white p-8 rounded-lg">
      <h2 className="text-3xl font-bold mb-8">Horario de atención</h2>
      
      <div className="space-y-4">
        {schedule.map((item) => (
          <div
            key={item.day}
            className="flex items-center justify-between pb-4 border-b border-gray-600 last:border-0"
          >
            <span className="font-medium">{item.day}</span>
            <div className="flex items-center gap-3">
              <span className="text-gray-400">—</span>
              <span className={item.hours === 'Closed' ? 'text-gray-400' : ''}>
                {item.hours}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Emergency Contact */}
      <div className="mt-8 pt-6 border-t border-gray-600">
        <div className="flex items-center gap-3">
          <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
          </svg>
          <div>
            <div className="text-sm text-gray-300">Emergencias</div>
            <div className="text-xl font-bold">(+502) 1122-3344</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ScheduleHours;
