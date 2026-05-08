import { createContext, useContext, useState } from 'react';
import type { FC, ReactNode } from 'react';

interface PatientHistoryContextType {
  patientId: string | null;
  patientName: string | null;
  setPatient: (id: string, name?: string) => void;
  clearPatient: () => void;
}

const PatientHistoryContext = createContext<PatientHistoryContextType>({
  patientId: null,
  patientName: null,
  setPatient: () => {},
  clearPatient: () => {},
});

export const PatientHistoryProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const [patientId, setPatientId] = useState<string | null>(null);
  const [patientName, setPatientName] = useState<string | null>(null);

  const setPatient = (id: string, name?: string) => {
    setPatientId(id);
    setPatientName(name ?? null);
  };

  const clearPatient = () => {
    setPatientId(null);
    setPatientName(null);
  };

  return (
    <PatientHistoryContext.Provider value={{ patientId, patientName, setPatient, clearPatient }}>
      {children}
    </PatientHistoryContext.Provider>
  );
};

export const usePatientHistory = () => useContext(PatientHistoryContext);
