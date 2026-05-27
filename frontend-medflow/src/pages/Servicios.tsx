/**
 * Servicios - MedFlow Hospital System
 * Página de servicios hospitalarios disponibles
 */

import type { FC } from 'react';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';

const Servicios: FC = () => {
  const services = [
    {
      id: 1,
      name: 'Consulta Médica General',
      description: 'Evaluación integral con médicos generales para diagnóstico y tratamiento de condiciones de salud.',
      icon: '👨‍⚕️',
    },
    {
      id: 3,
      name: 'Laboratorio Clínico',
      description: 'Análisis de sangre, estudios bioquímicos y pruebas diagnósticas especializadas.',
      icon: '🔬',
    },
    {
      id: 4,
      name: 'Farmacia',
      description: 'Dispensación de medicamentos y atención farmacéutica personalizada.',
      icon: '💊',
    },
    {
      id: 5,
      name: 'Triaje y Urgencias',
      description: 'Atención prioritaria de emergencias con sistema de triaje Manchester.',
      icon: '🚑',
    },
    {
      id: 6,
      name: 'Facturación y Cobros',
      description: 'Gestión transparente de costos y opciones de pago flexibles.',
      icon: '💳',
    },
  ];

  return (
    <div className="min-h-screen bg-white">
      <Navbar />

      {/* Hero Servicios */}
      <section className="bg-gradient-to-r from-medin-navy to-medin-navy-dark py-12 md:py-20 lg:py-28 px-4 md:px-6 lg:px-8">
        <div className="max-w-6xl lg:max-w-full mx-auto text-center xl:px-40 2xl:px-60">
          <h1 className="text-3xl md:text-5xl font-bold text-white mb-4 md:mb-6">
            Nuestros <span className="text-medin-cyan">Servicios</span>
          </h1>
          <p className="text-base md:text-xl text-gray-200 max-w-3xl mx-auto">
            Ofrecemos una amplia gama de servicios médicos de alta calidad para cuidar tu salud
          </p>
        </div>
      </section>

      {/* Servicios Grid */}
      <section className="py-12 md:py-20 px-4 md:px-6 lg:px-8 bg-white">
        <div className="max-w-6xl lg:max-w-full mx-auto xl:px-40 2xl:px-60">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8 lg:gap-10">
            {services.map((service) => (
              <div
                key={service.id}
                className="bg-white border-2 border-gray-100 rounded-lg p-6 md:p-8 hover:border-medin-cyan hover:shadow-lg transition-all hover:-translate-y-1"
              >
                <div className="text-4xl md:text-5xl mb-4">
                  {service.icon}
                </div>
                <h3 className="text-lg md:text-xl font-bold text-medin-navy mb-3 md:mb-4">
                  {service.name}
                </h3>
                <p className="text-sm md:text-base text-gray-600 leading-relaxed">
                  {service.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Descripción Detallada */}
      <section className="py-12 md:py-20 px-4 md:px-6 lg:px-8 bg-gray-50">
        <div className="max-w-6xl lg:max-w-full mx-auto xl:px-40 2xl:px-60">
          <h2 className="text-3xl md:text-4xl font-bold text-center text-medin-navy mb-12 md:mb-16">
            Comprometidos con tu Bienestar
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-12">
            <div className="bg-white rounded-lg p-6 md:p-8 shadow-md">
              <div className="w-12 h-12 md:w-14 md:h-14 bg-medin-cyan rounded-lg flex items-center justify-center mb-4 md:mb-6">
                <svg className="w-6 h-6 md:w-7 md:h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-lg md:text-xl font-bold text-medin-navy mb-3">Atención 24/7</h3>
              <p className="text-sm md:text-base text-gray-600">
                Disponible todos los días para tus emergencias médicas y consultas urgentes.
              </p>
            </div>

            <div className="bg-white rounded-lg p-6 md:p-8 shadow-md">
              <div className="w-12 h-12 md:w-14 md:h-14 bg-medin-blue rounded-lg flex items-center justify-center mb-4 md:mb-6">
                <svg className="w-6 h-6 md:w-7 md:h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3 className="text-lg md:text-xl font-bold text-medin-navy mb-3">Tecnología Avanzada</h3>
              <p className="text-sm md:text-base text-gray-600">
                Equipos modernos y sistemas de diagnóstico de última generación.
              </p>
            </div>

            <div className="bg-white rounded-lg p-6 md:p-8 shadow-md">
              <div className="w-12 h-12 md:w-14 md:h-14 bg-medin-cyan rounded-lg flex items-center justify-center mb-4 md:mb-6">
                <svg className="w-6 h-6 md:w-7 md:h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-lg md:text-xl font-bold text-medin-navy mb-3">Precios Accesibles</h3>
              <p className="text-sm md:text-base text-gray-600">
                Costos transparentes y opciones de pago adaptadas a tu situación.
              </p>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default Servicios;