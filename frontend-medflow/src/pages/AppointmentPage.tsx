/**
 * AppointmentPage - Página para agendar citas
 */

import type { FC } from 'react';
import PatientHeader from '../components/PatientHeader/PatientHeader';
import BookAppointmentForm from '../components/BookAppointmentForm/BookAppointmentForm';
import ScheduleHours from '../components/ScheduleHours/ScheduleHours';
import Footer from '../components/Footer/Footer';

const AppointmentPage: FC = () => {
  return (
    <div className="min-h-screen bg-white">
      <PatientHeader />

      {/* Hero */}
      <section className="relative bg-gradient-to-r from-gray-100 to-blue-50 py-8 md:py-16">
        <div className="hidden md:block absolute left-0 top-0 w-64 h-64 bg-medin-cyan opacity-10 rounded-full -translate-x-1/2 -translate-y-1/4" />
        <div className="max-w-7xl mx-auto px-4 md:px-6 relative">
          <h1 className="text-3xl md:text-5xl font-bold text-medin-navy">Agendar una cita</h1>
          <p className="mt-2 text-gray-600 text-sm md:text-base">
            Selecciona fecha, hora y describe brevemente el motivo de tu consulta.
          </p>
        </div>
      </section>

      {/* Content */}
      <section className="py-8 md:py-16">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-12 items-start">
            <BookAppointmentForm />
            <ScheduleHours />
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default AppointmentPage;
