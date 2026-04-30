/**
 * Componente HeroSection - MedFlow Hospital System
 * Sección principal con llamado a la acción
 */

import type { FC } from 'react';
import { Link } from 'react-router-dom';
import doctorImage from '../../assets/doctor.png';

const HeroSection: FC = () => {
  return (
    <section className="relative bg-gradient-to-r from-gray-50 to-blue-50 overflow-hidden px-4 md:px-8 lg:px-5">
      {/* Decorative Shape */}
      <div className="absolute left-0 top-0 w-96 h-96 bg-medin-cyan opacity-10 rounded-full -translate-x-1/2 -translate-y-1/4"></div>

      <div className="max-w-6xl lg:max-w-full mx-auto py-12 md:py-10 relative xl:px-40 2xl:px-50">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 md:gap-12 lg:gap-16 items-center">
          {/* Left Content */}
          <div className="space-y-4 md:space-y-6 text-center lg:text-left">
            {/* Subtitle */}
            <p className="text-medin-cyan font-semibold text-sm md:text-lg tracking-wide uppercase">
              Cuidando Vidas
            </p>

            {/* Main Title */}
            <h1 className="text-3xl md:text-5xl lg:text-6xl font-bold text-medin-navy leading-tight">
              Marcando el camino<br />
              en Excelencia Médica
            </h1>

          </div>

          {/* Right Content - Doctor Image */}
          <div className="relative order-first lg:order-last">
            <div className="relative z-10">
              {/* Doctor Image */}
              <div className="aspect-[5/4] overflow-hidden">
                <img 
                  src={doctorImage} 
                  alt="Doctor profesional" 
                  className="w-full h-full object-cover object-top"
                />
              </div>
            </div>

            {/* Decorative Elements - Hidden on mobile */}
            <div className="hidden md:block absolute -bottom-4 -right-4 w-72 h-72 bg-medin-cyan opacity-5 rounded-full"></div>
          </div>
        </div>

        {/* Appointment Button - Floating */}
        <div className="flex justify-center mt-8 md:mt-12">
          <Link
            to="/appointment"
            className="inline-flex items-center gap-2 md:gap-3 px-6 md:px-8 py-3 md:py-4 bg-medin-navy text-white rounded-lg font-semibold hover:bg-medin-navy-dark transition-all shadow-lg hover:shadow-xl transform hover:-translate-y-1 text-sm md:text-base"
          >
            <svg className="w-5 h-5 md:w-6 md:h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            Agendar Cita
          </Link>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
