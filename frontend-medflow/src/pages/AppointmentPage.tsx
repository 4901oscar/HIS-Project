/**
 * AppointmentPage - Página para agendar citas
 */

import type { FC } from 'react';
import Navbar from '../components/Navbar/Navbar';
import BookAppointmentForm from '../components/BookAppointmentForm/BookAppointmentForm';
import ScheduleHours from '../components/ScheduleHours/ScheduleHours';
import Footer from '../components/Footer/Footer';

const AppointmentPage: FC = () => {
  return (
    <div className="min-h-screen bg-white">
      <Navbar />

      {/* Hero Section with Breadcrumb */}
      <section className="relative bg-gradient-to-r from-gray-100 to-blue-50 py-8 md:py-16">
        {/* Decorative Shape - Hidden on mobile */}
        <div className="hidden md:block absolute left-0 top-0 w-64 h-64 bg-medin-cyan opacity-10 rounded-full -translate-x-1/2 -translate-y-1/4"></div>

        <div className="max-w-7xl mx-auto px-4 md:px-6 relative">
          <h1 className="text-3xl md:text-5xl font-bold text-medin-navy">
            Agendar una cita
          </h1>
        </div>
      </section>

      {/* Form Section */}
      <section className="py-8 md:py-16">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 md:gap-8">
            {/* Left Column - Form */}
            <div>
              <div className="bg-white mb-6 md:mb-8">
                <p className="text-xl text-gray-600 mb-4 md:mb-6">
                  Selecciona fecha, hora y describe brevemente el motivo de tu consulta.
                </p>
              </div>

              <BookAppointmentForm />
            </div>

            {/* Right Column - Schedule */}
            <div>
              <br />
              <br />
              <br />
              <br /> 
              <br />
              <ScheduleHours />
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default AppointmentPage;
