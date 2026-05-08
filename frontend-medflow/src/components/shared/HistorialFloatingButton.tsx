import { useState } from 'react';
import type { FC } from 'react';
import { usePatientHistory } from '../../context/PatientHistoryContext';
import HistorialModal from './HistorialModal';

const HistorialFloatingButton: FC = () => {
  const { patientId, patientName } = usePatientHistory();
  const [open, setOpen] = useState(false);

  if (!patientId) return null;

  return (
    <>
      <button
        onClick={() => setOpen(true)}
        title="Ver historial clinico"
        className="fixed bottom-6 right-6 z-40 flex items-center gap-2 bg-medin-navy text-white px-4 py-3 rounded-full shadow-lg hover:bg-medin-blue transition-colors text-sm font-medium"
      >
        <svg className="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        Historial
      </button>

      {open && (
        <HistorialModal
          patientId={patientId}
          patientName={patientName}
          onClose={() => setOpen(false)}
        />
      )}
    </>
  );
};

export default HistorialFloatingButton;
