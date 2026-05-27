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

    </div>
  );
};

export default ScheduleHours;
