/**
 * AppointmentPage - Página para agendar citas
 */

import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import BookAppointmentForm from '../components/BookAppointmentForm/BookAppointmentForm';
import ScheduleHours from '../components/ScheduleHours/ScheduleHours';
import Footer from '../components/Footer/Footer';
import { useAuth } from '../hooks/useAuth';

const AppointmentPage: FC = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/payment' } });
    }
  }, [isAuthenticated, navigate]);

  const handleSuccess = (message: string) => {
    setSuccessMessage(message);
    setErrorMessage(null);
    setTimeout(() => setSuccessMessage(null), 5000);
  };

  const handleError = (message: string) => {
    setErrorMessage(message);
    setSuccessMessage(null);
    setTimeout(() => setErrorMessage(null), 5000);
  };

  return (
    <div className="min-h-screen bg-white">
      <Navbar />

      {/* Hero Section with Breadcrumb */}
      <section className="relative bg-gradient-to-r from-gray-100 to-blue-50 py-8 md:py-16">
        {/* Decorative Shape - Hidden on mobile */}
        <div className="hidden md:block absolute left-0 top-0 w-64 h-64 bg-medin-cyan opacity-10 rounded-full -translate-x-1/2 -translate-y-1/4"></div>
        
        <div className="max-w-7xl mx-auto px-4 md:px-6 relative">

          {/* Title */}
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
                <h2 className="text-2xl md:text-3xl font-bold text-medin-navy mb-3 md:mb-4">
                  Agendar una cita
                </h2>
                <p className="text-sm md:text-base text-gray-600 mb-4 md:mb-6">
                  Selecciona fecha, hora y describe brevemente el motivo de tu consulta. Debes iniciar sesión para continuar.
                </p>
              </div>

              {/* Success Message */}
              {successMessage && (
                <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
                  <p className="text-green-800 font-medium">{successMessage}</p>
                </div>
              )}

              {/* Error Message */}
              {errorMessage && (
                <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-red-800 font-medium">{errorMessage}</p>
                </div>
              )}

              <BookAppointmentForm onSuccess={handleSuccess} onError={handleError} />
            </div>

            {/* Right Column - Schedule */}
            <div>
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
