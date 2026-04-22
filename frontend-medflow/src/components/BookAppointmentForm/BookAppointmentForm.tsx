import { useState, useEffect, useRef, useMemo } from 'react';
import type { ChangeEvent, FormEvent, FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { createAppointment, getAvailableSlotsForDate, holdSlot, releaseHold } from '../../services/appointmentService';
import { listActiveDoctors, getDoctorDaysOff, type Doctor, type DayOff } from '../../services/doctorService';

const HOLD_DURATION_SECONDS = 600; // 10 minutes
const PENDING_BOOKING_KEY = 'pending_booking';

/** Returns or creates a persistent session ID in localStorage. */
const getSessionId = (): string => {
  const key = 'booking_session_id';
  let id = localStorage.getItem(key);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(key, id);
  }
  return id;
};

/** Save the current selection before redirecting to login. */
const savePendingBooking = (date: string, time: string) => {
  sessionStorage.setItem(PENDING_BOOKING_KEY, JSON.stringify({ date, time }));
};

/** Read and clear the saved selection. */
const consumePendingBooking = (): { date: string; time: string } | null => {
  const raw = sessionStorage.getItem(PENDING_BOOKING_KEY);
  if (!raw) return null;
  sessionStorage.removeItem(PENDING_BOOKING_KEY);
  try { return JSON.parse(raw); } catch { return null; }
};

interface BookAppointmentFormProps {
  onSuccess?: (message: string) => void;
  onError?: (message: string) => void;
}

// ── helpers ──────────────────────────────────────────────────────────────────

const toDateStr = (year: number, month: number, day: number) =>
  `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

/** Generate 30-min slots from shiftStart (HH:mm) up to (but not including) shiftEnd.
 *  Handles overnight shifts (e.g. 16:00–00:00, 22:00–06:00). */
const shiftSlots = (start: string, end: string): string[] => {
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

const fmt = (t: string) => {
  const [h, m] = t.split(':').map(Number);
  const period = h < 12 ? 'AM' : 'PM';
  const dh = h === 0 ? 12 : h > 12 ? h - 12 : h;
  return `${dh}:${String(m).padStart(2, '0')} ${period}`;
};

// ── Calendar ─────────────────────────────────────────────────────────────────

const DAY_NAMES = ['Do', 'Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sa'];
const MONTH_NAMES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                     'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

interface CalendarProps {
  selected: string;
  onSelect: (date: string) => void;
  isBlocked: (date: string) => boolean;
}

const Calendar: FC<CalendarProps> = ({ selected, onSelect, isBlocked }) => {
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

// ── Main form ─────────────────────────────────────────────────────────────────

const BookAppointmentForm: FC<BookAppointmentFormProps> = ({ onSuccess, onError }) => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const sessionId = useMemo(() => getSessionId(), []);

  // Restore selection saved before login redirect
  const pending = useMemo(() => consumePendingBooking(), []);

  // ── data from backend (loaded once) ──
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [daysOffMap, setDaysOffMap] = useState<Record<string, string[]>>({});
  const [loadingData, setLoadingData] = useState(true);

  // ── form state ──
  const [date, setDate] = useState(pending?.date ?? '');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [selectedTime, setSelectedTime] = useState(pending?.time ?? '');
  const [motivo, setMotivo] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  // ── hold state ──
  const [holdSecondsLeft, setHoldSecondsLeft] = useState(0);
  const holdExpiresAt = useRef<number | null>(null); // epoch ms
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const isFirstDateLoad = useRef(true); // skip hold-release on initial pre-fill
  const didRestorePendingHold = useRef(false); // fire re-hold exactly once after restore

  // ── load doctors + days-off once on mount ──
  useEffect(() => {
    const load = async () => {
      try {
        const docs = await listActiveDoctors();
        setDoctors(docs);
        const offResults = await Promise.all(docs.map(d => getDoctorDaysOff(d.id)));
        const map: Record<string, string[]> = {};
        docs.forEach((d, i) => {
          map[d.id] = offResults[i].map((o: DayOff) => o.date.substring(0, 10));
        });
        setDaysOffMap(map);
      } catch {
        // silently ignore — form will still render, just without blocking
      } finally {
        setLoadingData(false);
      }
    };
    load();
  }, []);

  // ── derived: is a date fully blocked? ──
  const isBlocked = useMemo(() => (dateStr: string): boolean => {
    if (!doctors.length) return false;
    return doctors.every(d => (daysOffMap[d.id] ?? []).includes(dateStr));
  }, [doctors, daysOffMap]);

  // ── derived: shift slots for a date (doctors not on day-off that day) ──
  const shiftSlotsForDate = useMemo(() => (dateStr: string): string[] => {
    const available = doctors.filter(d => !(daysOffMap[d.id] ?? []).includes(dateStr));
    const set = new Set<string>();
    available.forEach(d => shiftSlots(d.shiftStart, d.shiftEnd).forEach(s => set.add(s)));
    return Array.from(set).sort();
  }, [doctors, daysOffMap]);

  // ── countdown timer ──
  const startTimer = () => {
    if (timerRef.current) clearInterval(timerRef.current);
    holdExpiresAt.current = Date.now() + HOLD_DURATION_SECONDS * 1000;
    setHoldSecondsLeft(HOLD_DURATION_SECONDS);
    timerRef.current = setInterval(() => {
      const left = Math.max(0, Math.round((holdExpiresAt.current! - Date.now()) / 1000));
      setHoldSecondsLeft(left);
      if (left === 0) {
        clearInterval(timerRef.current!);
        timerRef.current = null;
        // Hold expired — deselect and refresh available slots
        setSelectedTime('');
        if (date) {
          getAvailableSlotsForDate(date, sessionId)
            .then(setAvailableSlots)
            .catch(() => {});
        }
      }
    }, 1000);
  };

  const stopTimer = () => {
    if (timerRef.current) { clearInterval(timerRef.current); timerRef.current = null; }
    setHoldSecondsLeft(0);
    holdExpiresAt.current = null;
  };

  // Cleanup timer on unmount
  useEffect(() => () => { if (timerRef.current) clearInterval(timerRef.current); }, []);

  // ── when date changes: release hold + fetch available slots ──
  useEffect(() => {
    if (!date) { setAvailableSlots([]); setSelectedTime(''); stopTimer(); return; }
    // On the first run (pre-filled from login redirect), keep the selectedTime as-is
    if (!isFirstDateLoad.current && selectedTime) {
      releaseHold(sessionId).catch(() => {});
      setSelectedTime('');
      stopTimer();
    }
    isFirstDateLoad.current = false;
    setLoadingSlots(true);
    getAvailableSlotsForDate(date, sessionId)
      .then(slots => setAvailableSlots(slots))
      .catch(() => setAvailableSlots([]))
      .finally(() => setLoadingSlots(false));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date]);

  // Re-acquire hold + start timer after restoring pending booking from sessionStorage
  useEffect(() => {
    if (!pending || didRestorePendingHold.current || loadingSlots || !selectedTime || !date) return;
    didRestorePendingHold.current = true;
    holdSlot(sessionId, date, selectedTime).then(held => {
      if (held) {
        startTimer();
      } else {
        setSelectedTime('');
        setErrors(p => ({ ...p, time: 'El horario ya no está disponible. Selecciona otro.' }));
        getAvailableSlotsForDate(date, sessionId).then(setAvailableSlots).catch(() => {});
      }
    }).catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pending, loadingSlots, selectedTime, date]);

  // All slots to display = shift slots for selected date
  const displaySlots = date ? shiftSlotsForDate(date) : [];

  // ── slot selection with hold ──
  const handleSlotSelect = async (slot: string) => {
    if (errors.time) setErrors(p => ({ ...p, time: '' }));
    if (selectedTime === slot) return;
    setSelectedTime(slot);
    const held = await holdSlot(sessionId, date, slot);
    if (held) {
      startTimer();
    } else {
      // Slot taken by another session — refresh
      setSelectedTime('');
      stopTimer();
      setErrors(p => ({ ...p, time: 'Este horario acaba de ser tomado. Selecciona otro.' }));
      getAvailableSlotsForDate(date, sessionId).then(setAvailableSlots).catch(() => {});
    }
  };

  // ── form logic ──
  const validate = (): boolean => {
    const e: Record<string, string> = {};
    if (!date) e.date = 'Selecciona una fecha';
    if (!selectedTime) e.time = 'Selecciona un horario';
    if (!motivo.trim()) e.motivo = 'Describe el motivo de la consulta';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!isAuthenticated) {
      savePendingBooking(date, selectedTime);
      navigate('/login', { state: { from: '/appointment' } });
      return;
    }
    if (!validate()) return;
    setIsLoading(true);
    try {
      await createAppointment({
        patientId: user!.id,
        appointmentDate: date,
        appointmentTime: selectedTime,
        notes: motivo.trim(),
        sessionId,
      });
      stopTimer();
      onSuccess?.(
        `¡Cita agendada para el ${date} a las ${fmt(selectedTime)}! Se te asignará un doctor automáticamente.`
      );
      setDate(''); setAvailableSlots([]); setSelectedTime(''); setMotivo(''); setErrors({});
    } catch (err: any) {
      onError?.(err.response?.data?.message ?? 'No se pudo agendar la cita. Intenta de nuevo más tarde.');
    } finally {
      setIsLoading(false);
    }
  };

  const inputClass = (field: string) =>
    `w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan${errors[field] ? ' ring-2 ring-red-500' : ''}`;

  if (loadingData) {
    return <div className="text-gray-400 text-sm py-8 text-center">Cargando disponibilidad...</div>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">

      {/* Fecha (calendario custom) */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Fecha de la cita</label>
        <Calendar selected={date} onSelect={d => { setDate(d); if (errors.date) setErrors(p => ({ ...p, date: '' })); }} isBlocked={isBlocked} />
        {date && <p className="text-medin-cyan text-xs mt-2">Fecha seleccionada: {date}</p>}
        {errors.date && <p className="text-red-500 text-xs mt-1">{errors.date}</p>}
      </div>

      {/* Horarios */}
      {date && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Hora de la cita</label>
          {loadingSlots ? (
            <p className="text-sm text-gray-400">Verificando disponibilidad...</p>
          ) : displaySlots.length === 0 ? (
            <p className="text-sm text-amber-600">No hay horarios para esta fecha.</p>
          ) : (
            <div className="grid grid-cols-3 gap-2">
              {displaySlots.map(slot => {
                const isAvailable = availableSlots.includes(slot) || selectedTime === slot;
                const isSelected = selectedTime === slot;
                return (
                  <button
                    key={slot}
                    type="button"
                    disabled={!isAvailable}
                    onClick={() => handleSlotSelect(slot)}
                    className={`py-2 text-sm font-medium border transition-colors ${
                      isSelected
                        ? 'bg-medin-cyan text-medin-navy border-medin-cyan'
                        : isAvailable
                          ? 'bg-medin-navy text-white border-gray-600 hover:border-medin-cyan'
                          : 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed line-through'
                    }`}
                  >
                    {fmt(slot)}
                  </button>
                );
              })}
            </div>
          )}
          {/* Hold countdown */}
          {selectedTime && holdSecondsLeft > 0 && (
            <div className="mt-2 flex items-center gap-2 text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded px-3 py-2">
              <svg className="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Horario apartado por{' '}
              <span className="font-semibold tabular-nums">
                {String(Math.floor(holdSecondsLeft / 60)).padStart(2, '0')}:
                {String(holdSecondsLeft % 60).padStart(2, '0')}
              </span>
              — confirma tu cita antes de que expire.
            </div>
          )}
          {errors.time && <p className="text-red-500 text-xs mt-1">{errors.time}</p>}
        </div>
      )}

      {/* Motivo */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Motivo de consulta</label>
        <textarea
          value={motivo}
          onChange={(e: ChangeEvent<HTMLTextAreaElement>) => {
            setMotivo(e.target.value);
            if (errors.motivo) setErrors(p => ({ ...p, motivo: '' }));
          }}
          placeholder="Describe brevemente tus síntomas o el motivo de tu cita..."
          rows={4}
          className={`${inputClass('motivo')} resize-none`}
        />
        {errors.motivo && <p className="text-red-500 text-xs mt-1">{errors.motivo}</p>}
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="w-full py-3 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isLoading ? 'ENVIANDO...' : 'AGENDAR CITA'}
      </button>
    </form>
  );
};

export default BookAppointmentForm;
