/**
 * HomePage - MedFlow Hospital System
 * Página de inicio accesible para pacientes y empleados
 */

import type { FC } from 'react';
import Navbar from '../components/Navbar/Navbar';
import HeroSection from '../components/HeroSection/HeroSection';
import Footer from '../components/Footer/Footer';

const HomePage: FC = () => {
  return (
    <div className="min-h-screen bg-white">
      <Navbar />
      <HeroSection />
      
      {/* Sección de servicios o información adicional puede ir aquí */}
      <section className="py-8 md:py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <div className="text-center mb-8 md:mb-12">
            <h2 className="text-2xl md:text-4xl font-bold text-medin-navy mb-3 md:mb-4">
              Bienvenido a MedFlow
            </h2>
            <p className="text-sm md:text-lg text-gray-600 max-w-2xl mx-auto px-4">
              Sistema integral de gestión hospitalaria para pacientes y profesionales médicos
            </p>
          </div>

          {/* Quick Access Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 md:gap-8">

            {/* Pacientes */}
            <div className="bg-white rounded-lg shadow-md p-6 md:p-8 hover:shadow-xl transition-shadow flex flex-col">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-medin-cyan bg-opacity-10 rounded-full flex items-center justify-center mb-4 md:mb-6 mx-auto">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <h3 className="text-xl md:text-2xl font-bold text-medin-navy mb-2 md:mb-3 text-center">Pacientes</h3>
              <p className="text-sm md:text-base text-gray-600 mb-4 md:mb-6 flex-1 text-center">
                Agenda citas, consulta tu historial médico y gestiona tu atención de salud.
              </p>
              <a href="/appointment" className="block text-center px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-navy hover:text-white transition-colors text-sm">
                Agendar cita
              </a>
            </div>

            {/* Resultados */}
            <div className="bg-white rounded-lg shadow-md p-6 md:p-8 hover:shadow-xl transition-shadow flex flex-col">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-medin-navy bg-opacity-10 rounded-full flex items-center justify-center mb-4 md:mb-6 mx-auto">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-navy" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <h3 className="text-xl md:text-2xl font-bold text-medin-navy mb-2 md:mb-3 text-center">Resultados</h3>
              <p className="text-sm md:text-base text-gray-600 mb-4 md:mb-6 flex-1 text-center">
                Consulta resultados de laboratorio, imágenes médicas y reportes clínicos.
              </p>
              <a href="/login" className="block text-center px-4 py-2 bg-medin-navy text-white font-semibold rounded-lg hover:bg-medin-cyan hover:text-medin-navy transition-colors text-sm">
                Iniciar sesión
              </a>
            </div>

            {/* Atención */}
            <div className="bg-white rounded-lg shadow-md p-6 md:p-8 hover:shadow-xl transition-shadow flex flex-col">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-medin-cyan bg-opacity-10 rounded-full flex items-center justify-center mb-4 md:mb-6 mx-auto">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <h3 className="text-xl md:text-2xl font-bold text-medin-navy mb-2 md:mb-3 text-center">Atención</h3>
              <p className="text-sm md:text-base text-gray-600 mb-4 md:mb-6 flex-1 text-center">
                Gestión de citas, recursos hospitalarios y atención al paciente eficiente.
              </p>
              <a href="/servicios" className="block text-center px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-navy hover:text-white transition-colors text-sm">
                Ver servicios
              </a>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default HomePage;
