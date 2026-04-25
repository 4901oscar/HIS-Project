import { useState, useEffect, useRef, useCallback } from 'react';
import type { FC, FormEvent } from 'react';
import { MainLayout } from '../../components/Layout';
import {
  CalendarDaysIcon,
  QrCodeIcon,
  CheckCircleIcon,
} from '@heroicons/react/24/outline';
import { getPatientByDpi } from '../../services/patientService';
import type { CreatePatientRequest } from '../../services/patientService';
import { createPatientAccount } from '../../services/authService';
import type { CreatePatientAccountRequest } from '../../services/authService';
import { activateAppointment, listAllAppointments, scanAppointment, getAvailableSlotsForDate, createAppointment } from '../../services/appointmentService';
import type { AppointmentResponse, ScanResult } from '../../services/appointmentService';
import { listActiveDoctors, getDoctorDaysOff } from '../../services/doctorService';
import type { Doctor, DayOff } from '../../services/doctorService';
import { createInvoice } from '../../services/billingService';
import { Html5Qrcode } from 'html5-qrcode';
import axios from 'axios';

type Tab = 'schedule' | 'list' | 'scan';

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Agendada',
  ACTIVE: 'Activa',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  MISSED: 'Perdida',
};
const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
  MISSED: 'bg-orange-100 text-orange-800',
};

const emptyForm: CreatePatientRequest = {
  dpi: '', nit: '', firstName: '', secondName: '',
  firstLastName: '', secondLastName: '', birthDate: '',
  gender: 'MALE', email: '', phone: '',
  department: '', municipality: '', zone: '', address: '',
};

const SCANNER_ELEMENT_ID = 'qr-reader';
const DAY_NAMES = ['Do', 'Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sa'];
const MONTH_NAMES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                     'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

// ── Helpers ──────────────────────────────────────────────────────────────────

const toDateStr = (year: number, month: number, day: number) =>
  `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

const shiftSlots = (start: string, end: string): string[] => {
  const [sh, sm] = start.split(':').map(Number);
  const [eh, em] = end.split(':').map(Number);
  const startMin = sh * 60 + sm;
  let endMin = eh * 60 + em;
  if (endMin === 0) endMin = 24 * 60;
  if (endMin <= startMin) endMin += 24 * 60;
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

// ── Calendar Component ───────────────────────────────────────────────────────

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

  const firstDayOfWeek = new Date(viewYear, viewMonth, 1).getDay();
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();

  const prevMonth = () => {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11); }
    else setViewMonth(m => m - 1);
  };
  const nextMonth = () => {
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0); }
    else setViewMonth(m => m + 1);
  };

  const canGoPrev = viewYear > todayY || (viewYear === todayY && viewMonth > todayM);

  const cells: (number | null)[] = [
    ...Array(firstDayOfWeek).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];

  return (
    <div className="bg-medin-navy rounded-lg p-4 select-none">
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

      <div className="grid grid-cols-7 mb-2">
        {DAY_NAMES.map(d => (
          <div key={d} className="text-center text-xs text-gray-400 font-medium py-1">{d}</div>
        ))}
      </div>

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

// ── Main Component ───────────────────────────────────────────────────────────

const ActivateAppointments: FC = () => {
  const [tab, setTab] = useState<Tab>('schedule');

  // ── Lista de citas ────────────────────────────────────────────────────────
  const [allAppointments, setAllAppointments] = useState<AppointmentResponse[]>([]);
  const [listLoading, setListLoading] = useState(false);
  const [listLoaded, setListLoaded] = useState(false);
  const [activatingId, setActivatingId] = useState<string | null>(null);

  const loadAppointments = useCallback(() => {
    setListLoading(true);
    listAllAppointments()
      .then(setAllAppointments)
      .catch(() => {})
      .finally(() => { setListLoading(false); setListLoaded(true); });
  }, []);

  useEffect(() => {
    if (tab === 'list' && !listLoaded) {
      loadAppointments();
    }
  }, [tab, listLoaded, loadAppointments]);

  const handleActivateAppointment = async (appointmentId: string) => {
    setActivatingId(appointmentId);
    try {
      await activateAppointment(appointmentId);
      // Reload appointments to reflect the change
      loadAppointments();
    } catch (err) {
      console.error('Error activating appointment:', err);
      alert('Error al activar la cita');
    } finally {
      setActivatingId(null);
    }
  };

  // ── Agendar cita presencial ───────────────────────────────────────────────
  const [form, setForm] = useState<CreatePatientRequest>(emptyForm);
  const [patientExists, setPatientExists] = useState(false);
  const [dpiError, setDpiError] = useState<string | null>(null);
  const [checkingDpi, setCheckingDpi] = useState(false);

  // Date and time selection
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedTime, setSelectedTime] = useState('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [motivo, setMotivo] = useState('');

  // Doctors and days off
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [daysOffMap, setDaysOffMap] = useState<Record<string, string[]>>({});
  const [loadingData, setLoadingData] = useState(true);

  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [scheduleSuccess, setScheduleSuccess] = useState<string | null>(null);
  const [scheduleError, setScheduleError] = useState<string | null>(null);

  // Load doctors and days off
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
        // silently ignore
      } finally {
        setLoadingData(false);
      }
    };
    if (tab === 'schedule') {
      load();
    }
  }, [tab]);

  const isBlocked = useCallback((dateStr: string): boolean => {
    if (!doctors.length) return false;
    return doctors.every(d => (daysOffMap[d.id] ?? []).includes(dateStr));
  }, [doctors, daysOffMap]);

  const shiftSlotsForDate = useCallback((dateStr: string): string[] => {
    const available = doctors.filter(d => !(daysOffMap[d.id] ?? []).includes(dateStr));
    const set = new Set<string>();
    available.forEach(d => shiftSlots(d.shiftStart, d.shiftEnd).forEach(s => set.add(s)));
    return Array.from(set).sort();
  }, [doctors, daysOffMap]);

  const displaySlots = useCallback(() => {
    if (!selectedDate) return [];
    const slots = shiftSlotsForDate(selectedDate);
    
    const today = new Date();
    const selectedDateObj = new Date(selectedDate + 'T00:00:00');
    const isToday = 
      selectedDateObj.getFullYear() === today.getFullYear() &&
      selectedDateObj.getMonth() === today.getMonth() &&
      selectedDateObj.getDate() === today.getDate();
    
    if (!isToday) return slots;
    
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();
    const currentTimeInMinutes = currentHour * 60 + currentMinute;
    
    return slots.filter(slot => {
      const [slotHour, slotMinute] = slot.split(':').map(Number);
      const slotTimeInMinutes = slotHour * 60 + slotMinute;
      return slotTimeInMinutes >= currentTimeInMinutes + 30;
    });
  }, [selectedDate, shiftSlotsForDate]);

  // Check DPI on blur
  const handleDpiBlur = async () => {
    const dpi = form.dpi.trim();
    if (!dpi || dpi.length !== 13) return;
    
    setCheckingDpi(true);
    setDpiError(null);
    setPatientExists(false);

    try {
      const patient = await getPatientByDpi(dpi);
      setPatientExists(true);
      // Fill form with existing patient data
      setForm({
        dpi: patient.dpi,
        nit: patient.nit || '',
        firstName: patient.firstName,
        secondName: patient.secondName || '',
        firstLastName: patient.firstLastName,
        secondLastName: patient.secondLastName || '',
        birthDate: patient.birthDate,
        gender: patient.gender as 'MALE' | 'FEMALE' | 'OTHER',
        email: patient.email,
        phone: patient.phone,
        department: patient.department || '',
        municipality: patient.municipality || '',
        zone: '',
        address: patient.address || '',
      });
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 404) {
        setDpiError('DPI no registrado');
        setPatientExists(false);
      } else {
        setDpiError('Error al verificar DPI');
      }
    } finally {
      setCheckingDpi(false);
    }
  };

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  // Load available slots when date changes
  useEffect(() => {
    if (!selectedDate) {
      setAvailableSlots([]);
      setSelectedTime('');
      return;
    }
    setLoadingSlots(true);
    getAvailableSlotsForDate(selectedDate)
      .then(slots => setAvailableSlots(slots))
      .catch(() => setAvailableSlots([]))
      .finally(() => setLoadingSlots(false));
  }, [selectedDate]);

  const handleScheduleAppointment = async (e: FormEvent) => {
    e.preventDefault();
    if (!selectedDate || !selectedTime || !motivo.trim()) {
      setScheduleError('Por favor completa todos los campos requeridos');
      return;
    }

    setScheduleLoading(true);
    setScheduleError(null);
    setScheduleSuccess(null);

    try {
      let targetPatientId: string | undefined;

      // If patient doesn't exist, create it first via auth-service
      if (!patientExists) {
        const accountRequest: CreatePatientAccountRequest = {
          dpi: form.dpi,
          nit: form.nit || undefined,
          firstName: form.firstName,
          secondName: form.secondName || undefined,
          firstLastName: form.firstLastName,
          secondLastName: form.secondLastName || undefined,
          email: form.email,
          phone: form.phone,
          birthDate: form.birthDate,
          gender: form.gender === 'MALE' ? 'M' : form.gender === 'FEMALE' ? 'F' : 'M',
          department: form.department || undefined,
          municipality: form.municipality || undefined,
          zone: form.zone || undefined,
          address: form.address || undefined,
        };
        const accountResponse = await createPatientAccount(accountRequest);
        targetPatientId = accountResponse.patientId;
      } else {
        // Patient exists, get their ID by DPI
        const existingPatient = await getPatientByDpi(form.dpi);
        targetPatientId = existingPatient.id;
      }

      // Create appointment with patientId
      await createAppointment({
        patientId: targetPatientId,
        appointmentDate: selectedDate,
        appointmentTime: selectedTime,
        notes: motivo.trim(),
      });

      // Create invoice for consultation fee
      await createInvoice({
        patientId: targetPatientId,
        charges: [
          {
            type: 'CONSULTATION',
            description: 'Consulta General',
            quantity: 1,
            unitPrice: 150.00, // Precio de consulta general
          },
        ],
      });

      setScheduleSuccess('Cita agendada exitosamente. Se ha enviado un correo con los detalles. El paciente debe pasar a caja para realizar el pago.');
      
      // Reset form
      setForm(emptyForm);
      setSelectedDate('');
      setSelectedTime('');
      setMotivo('');
      setPatientExists(false);
      setDpiError(null);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setScheduleError(err.response?.data?.message || err.response?.data?.error || 'Error al agendar la cita');
      } else {
        setScheduleError('Error al conectar con el servidor');
      }
    } finally {
      setScheduleLoading(false);
    }
  };

  // ── Escanear QR ───────────────────────────────────────────────────────────
  const scannerRef = useRef<Html5Qrcode | null>(null);
  const [scannerActive, setScannerActive] = useState(false);
  const [scannerError, setScannerError] = useState<string | null>(null);
  const [scanResult, setScanResult] = useState<ScanResult | null>(null);
  const [scanLoading, setScanLoading] = useState(false);

  const stopScanner = useCallback(async () => {
    if (scannerRef.current) {
      try { await scannerRef.current.stop(); } catch { /* already stopped */ }
      scannerRef.current.clear();
      scannerRef.current = null;
    }
    setScannerActive(false);
  }, []);

  const startScanner = useCallback(async () => {
    setScanResult(null);
    setScannerError(null);
    const scanner = new Html5Qrcode(SCANNER_ELEMENT_ID);
    scannerRef.current = scanner;
    try {
      await scanner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 250, height: 250 } },
        async (decodedText) => {
          await stopScanner();
          let appointmentId = '';
          try {
            const data = JSON.parse(decodedText);
            appointmentId = data.appointmentId ?? data.id ?? '';
          } catch {
            appointmentId = decodedText.trim();
          }
          if (!appointmentId) {
            setScannerError('QR inválido — no contiene ID de cita.');
            return;
          }
          setScanLoading(true);
          try {
            const result = await scanAppointment(appointmentId);
            setScanResult(result);
          } catch (err) {
            if (axios.isAxiosError(err) && err.response?.status === 404) {
              setScannerError('Cita no encontrada.');
            } else {
              setScannerError('Error al procesar el QR. Intenta de nuevo.');
            }
          } finally {
            setScanLoading(false);
          }
        },
        () => { /* ignore per-frame errors */ }
      );
      setScannerActive(true);
    } catch {
      setScannerError('No se pudo acceder a la cámara. Verifica los permisos.');
      scannerRef.current = null;
    }
  }, [stopScanner]);

  useEffect(() => {
    if (tab !== 'scan') { stopScanner(); }
  }, [tab, stopScanner]);

  useEffect(() => () => { stopScanner(); }, [stopScanner]);

  const scanStatusStyle: Record<string, string> = {
    ACTIVE: 'bg-green-50 border-green-300',
    EARLY:  'bg-yellow-50 border-yellow-300',
    MISSED: 'bg-red-50 border-red-300',
  };
  const scanStatusIcon: Record<string, string> = {
    ACTIVE: '✅', EARLY: '⏰', MISSED: '❌',
  };

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Admisión</h2>
          <p className="mt-1 text-sm text-gray-600">Agendar citas presenciales y gestión de citas</p>
        </div>

        {/* Tabs */}
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-6 overflow-x-auto">
            {([
              { key: 'schedule', label: 'Agendar Cita Presencial', Icon: CalendarDaysIcon },
              { key: 'list',     label: 'Ver Citas',              Icon: CalendarDaysIcon },
              { key: 'scan',     label: 'Escanear QR',            Icon: QrCodeIcon },
            ] as const).map(({ key, label, Icon }) => (
              <button
                key={key}
                onClick={() => setTab(key)}
                className={`py-3 px-1 border-b-2 font-medium text-sm flex items-center gap-2 whitespace-nowrap ${
                  tab === key
                    ? 'border-medin-cyan text-medin-cyan'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                <Icon className="h-4 w-4" />
                {label}
              </button>
            ))}
          </nav>
        </div>

        {/* ── Tab: Agendar Cita Presencial ── */}
        {tab === 'schedule' && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Datos del Paciente y Cita</h3>
            
            {scheduleSuccess && (
              <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-green-800 font-medium">{scheduleSuccess}</p>
              </div>
            )}
            
            {scheduleError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{scheduleError}</div>
            )}

            {loadingData ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-7 w-7 border-4 border-medin-cyan border-t-transparent"></div>
                <p className="text-sm text-gray-500 mt-2">Cargando disponibilidad...</p>
              </div>
            ) : (
              <form onSubmit={handleScheduleAppointment} className="space-y-6">
                {/* Patient Information */}
                <div className="space-y-4">
                  <h4 className="font-medium text-gray-900">Información del Paciente</h4>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className={labelClass}>DPI <span className="text-red-500">*</span></label>
                      <input 
                        name="dpi" 
                        value={form.dpi} 
                        onChange={(e) => {
                          handleFormChange(e);
                          // Reset patient state when DPI changes
                          if (patientExists) {
                            setPatientExists(false);
                            setDpiError(null);
                          }
                        }}
                        onBlur={handleDpiBlur}
                        required 
                        maxLength={13} 
                        minLength={13} 
                        placeholder="13 dígitos" 
                        className={inputClass}
                      />
                      {checkingDpi && <p className="text-xs text-gray-500 mt-1">Verificando DPI...</p>}
                      {dpiError && <p className="text-xs text-red-600 mt-1">{dpiError}</p>}
                      {patientExists && <p className="text-xs text-green-600 mt-1">✓ Paciente encontrado</p>}
                    </div>
                    
                    <div>
                      <label className={labelClass}>NIT</label>
                      <input 
                        name="nit" 
                        value={form.nit} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Primer Nombre <span className="text-red-500">*</span></label>
                      <input 
                        name="firstName" 
                        value={form.firstName} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        required 
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Segundo Nombre</label>
                      <input 
                        name="secondName" 
                        value={form.secondName} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Primer Apellido <span className="text-red-500">*</span></label>
                      <input 
                        name="firstLastName" 
                        value={form.firstLastName} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        required 
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Segundo Apellido</label>
                      <input 
                        name="secondLastName" 
                        value={form.secondLastName} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Fecha de Nacimiento <span className="text-red-500">*</span></label>
                      <input 
                        type="date" 
                        name="birthDate" 
                        value={form.birthDate} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        required 
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Género <span className="text-red-500">*</span></label>
                      <select 
                        name="gender" 
                        value={form.gender} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        required 
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      >
                        <option value="MALE">Masculino</option>
                        <option value="FEMALE">Femenino</option>
                        <option value="OTHER">Otro</option>
                      </select>
                    </div>
                    
                    <div>
                      <label className={labelClass}>Correo Electrónico <span className="text-red-500">*</span></label>
                      <input 
                        type="email" 
                        name="email" 
                        value={form.email} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        required 
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Teléfono <span className="text-red-500">*</span></label>
                      <input 
                        name="phone" 
                        value={form.phone} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        required 
                        maxLength={8} 
                        minLength={8} 
                        placeholder="8 dígitos" 
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Departamento</label>
                      <input 
                        name="department" 
                        value={form.department} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Municipio</label>
                      <input 
                        name="municipality" 
                        value={form.municipality} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Zona</label>
                      <input 
                        name="zone" 
                        value={form.zone} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                    
                    <div>
                      <label className={labelClass}>Dirección</label>
                      <input 
                        name="address" 
                        value={form.address} 
                        onChange={handleFormChange}
                        disabled={patientExists}
                        className={`${inputClass} ${patientExists ? 'bg-gray-100' : ''}`}
                      />
                    </div>
                  </div>
                </div>

                {/* Appointment Date and Time */}
                <div className="space-y-4 border-t pt-4">
                  <h4 className="font-medium text-gray-900">Fecha y Hora de la Cita</h4>
                  
                  <div>
                    <label className={labelClass}>Fecha de la cita <span className="text-red-500">*</span></label>
                    <Calendar 
                      selected={selectedDate} 
                      onSelect={setSelectedDate} 
                      isBlocked={isBlocked} 
                    />
                    {selectedDate && <p className="text-medin-cyan text-xs mt-2">Fecha seleccionada: {selectedDate}</p>}
                  </div>

                  {selectedDate && (
                    <div>
                      <label className={labelClass}>Hora de la cita <span className="text-red-500">*</span></label>
                      {loadingSlots ? (
                        <p className="text-sm text-gray-400">Verificando disponibilidad...</p>
                      ) : displaySlots().length === 0 ? (
                        <p className="text-sm text-amber-600">No hay horarios disponibles para esta fecha.</p>
                      ) : (
                        <div className="grid grid-cols-3 gap-2">
                          {displaySlots().map(slot => {
                            const isAvailable = availableSlots.includes(slot) || selectedTime === slot;
                            const isSelected = selectedTime === slot;
                            return (
                              <button
                                key={slot}
                                type="button"
                                disabled={!isAvailable}
                                onClick={() => setSelectedTime(slot)}
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
                    </div>
                  )}

                  <div>
                    <label className={labelClass}>Motivo de consulta <span className="text-red-500">*</span></label>
                    <textarea
                      value={motivo}
                      onChange={(e) => setMotivo(e.target.value)}
                      placeholder="Describe brevemente el motivo de la consulta..."
                      rows={3}
                      required
                      className={`${inputClass} resize-none`}
                    />
                  </div>
                </div>

                <div className="flex justify-end pt-2">
                  <button 
                    type="submit" 
                    disabled={scheduleLoading || !selectedDate || !selectedTime}
                    className="px-6 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {scheduleLoading ? 'Agendando...' : 'Agendar Cita'}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}

        {/* ── Tab: Ver Citas ── */}
        {tab === 'list' && (
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Todas las Citas</h3>
              <button 
                onClick={() => { setListLoaded(false); loadAppointments(); }} 
                className="text-xs text-medin-cyan hover:underline"
              >
                Actualizar
              </button>
            </div>
            
            {listLoading ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-7 w-7 border-4 border-medin-cyan border-t-transparent"></div>
              </div>
            ) : allAppointments.length === 0 ? (
              <p className="text-gray-500 text-sm">No hay citas registradas.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
                      <th className="pb-2 pr-4">Fecha</th>
                      <th className="pb-2 pr-4">Hora</th>
                      <th className="pb-2 pr-4">Estado</th>
                      <th className="pb-2 pr-4">Motivo</th>
                      <th className="pb-2 pr-4">ID Paciente</th>
                      <th className="pb-2 pr-4">ID Cita</th>
                      <th className="pb-2">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {allAppointments
                      .slice()
                      .sort((a, b) => a.appointmentDate.localeCompare(b.appointmentDate) || a.appointmentTime.localeCompare(b.appointmentTime))
                      .map((appt) => (
                        <tr key={appt.id} className="hover:bg-gray-50">
                          <td className="py-2 pr-4 whitespace-nowrap">{appt.appointmentDate}</td>
                          <td className="py-2 pr-4 whitespace-nowrap">{appt.appointmentTime.substring(0, 5)}</td>
                          <td className="py-2 pr-4">
                            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[appt.status] ?? 'bg-gray-100 text-gray-700'}`}>
                              {STATUS_LABEL[appt.status] ?? appt.status}
                            </span>
                          </td>
                          <td className="py-2 pr-4 max-w-xs truncate">{appt.notes ?? '—'}</td>
                          <td className="py-2 pr-4 font-mono text-xs">{appt.patientId}</td>
                          <td className="py-2 pr-4 font-mono text-xs">{appt.id}</td>
                          <td className="py-2">
                            {appt.status === 'SCHEDULED' ? (
                              <button
                                onClick={() => handleActivateAppointment(appt.id)}
                                disabled={activatingId === appt.id}
                                className="flex items-center gap-1 px-3 py-1 bg-medin-cyan text-medin-navy text-xs font-medium rounded hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                {activatingId === appt.id ? (
                                  <>
                                    <div className="animate-spin rounded-full h-3 w-3 border-2 border-medin-navy border-t-transparent"></div>
                                    Activando...
                                  </>
                                ) : (
                                  <>
                                    <CheckCircleIcon className="h-3 w-3" />
                                    Activar
                                  </>
                                )}
                              </button>
                            ) : (
                              <span className="text-xs text-gray-400">—</span>
                            )}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* ── Tab: Escanear QR ── */}
        {tab === 'scan' && (
          <div className="bg-white rounded-lg shadow p-6 max-w-md mx-auto">
            <h3 className="text-lg font-semibold text-gray-900 mb-1">Escanear QR de Cita</h3>
            <p className="text-sm text-gray-500 mb-4">Apunta la cámara al QR del paciente para validar y activar la cita.</p>

            <div id={SCANNER_ELEMENT_ID} className="rounded-lg overflow-hidden mb-4" />

            <div className="flex gap-3 mb-4">
              {!scannerActive ? (
                <button
                  onClick={startScanner}
                  className="flex-1 py-2.5 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors flex items-center justify-center gap-2"
                >
                  <QrCodeIcon className="h-5 w-5" />
                  Iniciar cámara
                </button>
              ) : (
                <button
                  onClick={stopScanner}
                  className="flex-1 py-2.5 bg-gray-200 text-gray-700 font-semibold rounded-lg hover:bg-gray-300 transition-colors"
                >
                  Detener cámara
                </button>
              )}
              {scanResult && (
                <button
                  onClick={() => { setScanResult(null); setScannerError(null); startScanner(); }}
                  className="flex-1 py-2.5 bg-medin-navy text-white font-semibold rounded-lg hover:bg-medin-navy/90 transition-colors"
                >
                  Escanear otro
                </button>
              )}
            </div>

            {scanLoading && (
              <div className="flex items-center gap-2 text-sm text-gray-600 mb-3">
                <div className="animate-spin rounded-full h-4 w-4 border-2 border-medin-cyan border-t-transparent" />
                Validando cita...
              </div>
            )}

            {scannerError && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">
                {scannerError}
              </div>
            )}

            {scanResult && (
              <div className={`p-4 border rounded-lg ${scanStatusStyle[scanResult.status] ?? 'bg-gray-50 border-gray-200'}`}>
                <div className="flex items-center gap-2 mb-3">
                  <span className="text-2xl">{scanStatusIcon[scanResult.status]}</span>
                  <span className="font-bold text-gray-900 text-lg">{scanResult.message}</span>
                </div>
                <div className="space-y-1 text-sm text-gray-700">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Fecha</span>
                    <span className="font-medium">{scanResult.date}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Hora</span>
                    <span className="font-medium">{scanResult.time.substring(0, 5)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Estado</span>
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[scanResult.appointmentStatus] ?? ''}`}>
                      {STATUS_LABEL[scanResult.appointmentStatus] ?? scanResult.appointmentStatus}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">ID Cita</span>
                    <span className="font-mono text-xs">{scanResult.appointmentId}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default ActivateAppointments;
