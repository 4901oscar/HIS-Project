import { useState, useEffect, useRef, useMemo } from 'react';
import type { ChangeEvent, FormEvent, FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { getAvailableSlotsForDate, holdSlot, releaseHold } from '../../services/appointmentService';
import { listActiveDoctors, getDoctorDaysOff, type Doctor, type DayOff } from '../../services/doctorService';
import { Calendar, shiftSlots, fmt } from '../Calendar';

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

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
interface BookAppointmentFormProps {}

// ── Main form ─────────────────────────────────────────────────────────────────

const BookAppointmentForm: FC<BookAppointmentFormProps> = () => {
  const { isAuthenticated } = useAuth();
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

  // All slots to display = shift slots for selected date, filtered by current time if today
  const displaySlots = useMemo(() => {
    if (!date) return [];
    const slots = shiftSlotsForDate(date);
    
    // If selected date is today, filter out past time slots
    const today = new Date();
    const selectedDate = new Date(date + 'T00:00:00');
    const isToday = 
      selectedDate.getFullYear() === today.getFullYear() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getDate() === today.getDate();
    
    if (!isToday) return slots;
    
    // Filter out slots that have already passed
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();
    const currentTimeInMinutes = currentHour * 60 + currentMinute;
    
    return slots.filter(slot => {
      const [slotHour, slotMinute] = slot.split(':').map(Number);
      const slotTimeInMinutes = slotHour * 60 + slotMinute;
      // Keep slots that are at least 30 minutes in the future
      return slotTimeInMinutes >= currentTimeInMinutes + 30;
    });
  }, [date, shiftSlotsForDate]);

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
    stopTimer();
    navigate('/payment', {
      state: {
        date,
        time: selectedTime,
        notes: motivo.trim(),
        sessionId,
      },
    });
  };

  const inputClass = (field: string) =>
    `w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan${errors[field] ? ' ring-2 ring-red-500' : ''}`;

  if (loadingData) {
    return <div className="text-gray-400 text-xl py-8 text-center">Cargando disponibilidad...</div>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">

      {/* Fecha (calendario custom) */}
      <div>
        <label className="block text-lg font-medium text-gray-700 mb-2">Fecha de la cita</label>
        <Calendar selected={date} onSelect={d => { setDate(d); if (errors.date) setErrors(p => ({ ...p, date: '' })); }} isBlocked={isBlocked} />
        {date && <p className="text-medin-cyan text-lg mt-2">Fecha seleccionada: {date}</p>}
        {errors.date && <p className="text-red-500 text-lg mt-1">{errors.date}</p>}
      </div>

      {/* Horarios */}
      {date && (
        <div>
          <label className="block text-lg font-medium text-gray-700 mb-2">Hora de la cita</label>
          {loadingSlots ? (
            <p className="text-lg text-gray-400">Verificando disponibilidad...</p>
          ) : displaySlots.length === 0 ? (
            <p className="text-lg text-amber-600">No hay horarios para esta fecha.</p>
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
                    className={`py-2 text-lg font-medium border transition-colors ${
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
            <div className="mt-2 flex items-center gap-2 text-base text-amber-700 bg-amber-50 border border-amber-200 rounded px-3 py-2">
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
          {errors.time && <p className="text-red-500 text-lg mt-1">{errors.time}</p>}
        </div>
      )}

      {/* Motivo */}
      <div>
        <label className="block text-lg font-medium text-gray-700 mb-1">Motivo de consulta</label>
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
        {errors.motivo && <p className="text-red-500 text-lg mt-1">{errors.motivo}</p>}
      </div>

      <button
        type="submit"
        className="w-full py-3 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors"
      >
        AGENDAR CITA
      </button>
    </form>
  );
};

export default BookAppointmentForm;
