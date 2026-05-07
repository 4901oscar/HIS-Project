import { useState } from 'react';
import type { FC } from 'react';

// ── Constants ─────────────────────────────────────────────────────────────────

export const DAY_NAMES = ['Do', 'Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sa'];
export const MONTH_NAMES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                     'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

// ── Helper functions ──────────────────────────────────────────────────────────

export const toDateStr = (year: number, month: number, day: number) =>
  `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

/** Generate 30-min slots from shiftStart (HH:mm) up to (but not including) shiftEnd.
 *  Handles overnight shifts (e.g. 16:00–00:00, 22:00–06:00). */
export const shiftSlots = (start: string, end: string): string[] => {
  const [sh, sm] = start.split(':').map(Number);
  const [eh, em] = end.split(':').map(Number);
  const startMin = sh * 60 + sm;
  let endMin = eh * 60 + em;
  if (endMin === 0) endMin = 24 * 60;         // 00:00 = fin de día
  if (endMin <= startMin) endMin += 24 * 60;  // turno nocturno
  const slots: string[] = [];
  for (let m = startMin; m < endMin; m += 30) {
    const actual = m % (24 * 60);
    const h = Math.floor(actual / 60);
    const min = actual % 60;
    slots.push(`${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`);
  }
  return slots;
};

export const fmt = (t: string) => {
  const [h, m] = t.split(':').map(Number);
  const period = h < 12 ? 'AM' : 'PM';
  const dh = h === 0 ? 12 : h > 12 ? h - 12 : h;
  return `${dh}:${String(m).padStart(2, '0')} ${period}`;
};

// ── Calendar Component ────────────────────────────────────────────────────────

interface CalendarProps {
  selected: string;
  onSelect: (date: string) => void;
  isBlocked: (date: string) => boolean;
}

export const Calendar: FC<CalendarProps> = ({ selected, onSelect, isBlocked }) => {
  const today = new Date();
  const [viewYear, setViewYear] = useState(today.getFullYear());
  const [viewMonth, setViewMonth] = useState(today.getMonth());

  const todayY = today.getFullYear();
  const todayM = today.getMonth();
  const todayD = today.getDate();

  const firstDayOfWeek = new Date(viewYear, viewMonth, 1).getDay(); // 0=Sun
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();

  const prevMonth = () => {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11); }
    else setViewMonth(m => m - 1);
  };
  const nextMonth = () => {
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0); }
    else setViewMonth(m => m + 1);
  };

  // Disable navigation to past months
  const canGoPrev = viewYear > todayY || (viewYear === todayY && viewMonth > todayM);

  const cells: (number | null)[] = [
    ...Array(firstDayOfWeek).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];

  return (
    <div className="bg-medin-navy rounded-lg p-4 select-none">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <button
          type="button"
          onClick={prevMonth}
          disabled={!canGoPrev}
          className="p-1 text-white hover:text-medin-cyan disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          &#8249;
        </button>
        <span className="text-white font-semibold text-sm">
          {MONTH_NAMES[viewMonth]} {viewYear}
        </span>
        <button
          type="button"
          onClick={nextMonth}
          className="p-1 text-white hover:text-medin-cyan transition-colors"
        >
          &#8250;
        </button>
      </div>

      {/* Day names */}
      <div className="grid grid-cols-7 mb-2">
        {DAY_NAMES.map(d => (
          <div key={d} className="text-center text-xs text-gray-400 font-medium py-1">{d}</div>
        ))}
      </div>

      {/* Days */}
      <div className="grid grid-cols-7 gap-1">
        {cells.map((day, idx) => {
          if (day === null) return <div key={`e-${idx}`} />;

          const dateStr = toDateStr(viewYear, viewMonth, day);
          const isPast =
            viewYear < todayY ||
            (viewYear === todayY && viewMonth < todayM) ||
            (viewYear === todayY && viewMonth === todayM && day < todayD);
          const blocked = isBlocked(dateStr);
          const isSelected = selected === dateStr;
          const isToday = viewYear === todayY && viewMonth === todayM && day === todayD;
          const disabled = isPast || blocked;

          return (
            <button
              key={day}
              type="button"
              disabled={disabled}
              onClick={() => onSelect(dateStr)}
              className={`
                relative text-xs rounded py-1.5 font-medium transition-colors
                ${isSelected
                  ? 'bg-medin-cyan text-medin-navy'
                  : disabled
                    ? 'text-gray-600 cursor-not-allowed line-through'
                    : 'text-white hover:bg-white/10'}
                ${isToday && !isSelected ? 'ring-1 ring-medin-cyan' : ''}
              `}
            >
              {day}
            </button>
          );
        })}
      </div>
    </div>
  );
};
