/**
 * Nosotros - MedFlow Hospital System
 * Página de inicio accesible para pacientes y empleados
 */

import type { FC } from 'react';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';

const Nosotros: FC = () => {
  return (
    <div className="min-h-screen bg-white">
      <Navbar />
        
      {/* Hero Nosotros */}
      <section className="bg-gradient-to-r from-medin-navy to-medin-navy-dark py-12 md:py-20 lg:py-28 px-4 md:px-6 lg:px-8">
        <div className="max-w-6xl lg:max-w-full mx-auto text-center xl:px-40 2xl:px-60">
          <h1 className="text-3xl md:text-5xl font-bold text-white mb-4 md:mb-6">
            Sobre <span className="text-medin-cyan">MedFlow</span>
          </h1>
          <p className="text-base md:text-xl text-gray-200 max-w-3xl mx-auto">
            Transformando la atención hospitalaria con tecnología moderna y accesible para todos
          </p>
        </div>
      </section>

      {/* Sección: Quiénes Somos */}
      <section className="py-12 md:py-20 px-4 md:px-6 lg:px-8 bg-white">
        <div className="max-w-6xl lg:max-w-full mx-auto xl:px-40 2xl:px-60">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 md:gap-12 lg:gap-16 items-center">
            <div>
              <h2 className="text-2xl md:text-4xl font-bold text-medin-navy mb-4 md:mb-6">
                ¿Quiénes Somos?
              </h2>
              <p className="text-base md:text-lg text-gray-700 mb-4 leading-relaxed">
                MedFlow es un sistema integral de gestión hospitalaria diseñado para modernizar la atención médica. Nuestro objetivo es facilitar la comunicación entre pacientes, médicos y el personal administrativo mediante una plataforma única, segura y eficiente.
              </p>
              <p className="text-base md:text-lg text-gray-700 leading-relaxed">
                Creemos que la tecnología debe simplificar, no complicar. Por eso MedFlow pone el enfoque en la experiencia del usuario, garantizando que cada interacción sea intuitiva y significativa.
              </p>
            </div>
            <div className="bg-medin-navy bg-opacity-5 rounded-lg p-8 md:p-12">
              <div className="space-y-6">
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-10 h-10 md:w-12 md:h-12 bg-medin-cyan rounded-full flex items-center justify-center">
                    <svg className="w-5 h-5 md:w-6 md:h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-bold text-medin-navy">Accesibilidad</h3>
                    <p className="text-sm text-gray-600">Disponible en web y dispositivos móviles</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-10 h-10 md:w-12 md:h-12 bg-medin-cyan rounded-full flex items-center justify-center">
                    <svg className="w-5 h-5 md:w-6 md:h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-bold text-medin-navy">Seguridad</h3>
                    <p className="text-sm text-gray-600">Protección de datos con estándares de salud</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-10 h-10 md:w-12 md:h-12 bg-medin-cyan rounded-full flex items-center justify-center">
                    <svg className="w-5 h-5 md:w-6 md:h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-bold text-medin-navy">Eficiencia</h3>
                    <p className="text-sm text-gray-600">Reduce tiempos y optimiza procesos</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Sección: Misión, Visión, Valores */}
      <section className="py-12 md:py-20 px-4 md:px-6 lg:px-8 bg-gray-50">
        <div className="max-w-6xl lg:max-w-full mx-auto xl:px-40 2xl:px-60">
          <h2 className="text-3xl md:text-4xl font-bold text-center text-medin-navy mb-12 md:mb-16">
            Nuestra Propuesta
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 lg:gap-12">
            {/* Misión */}
            <div className="bg-white rounded-lg shadow-md p-6 md:p-8 hover:shadow-lg transition-shadow">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-medin-cyan bg-opacity-10 rounded-lg flex items-center justify-center mb-4 md:mb-6">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3 className="text-xl md:text-2xl font-bold text-medin-navy mb-3 md:mb-4">Misión</h3>
              <p className="text-sm md:text-base text-gray-600 leading-relaxed">
                Proporcionar una plataforma tecnológica que simplifique la gestión hospitalaria y mejore la calidad de atención médica para todos.
              </p>
            </div>

            {/* Visión */}
            <div className="bg-white rounded-lg shadow-md p-6 md:p-8 hover:shadow-lg transition-shadow">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-medin-navy bg-opacity-10 rounded-lg flex items-center justify-center mb-4 md:mb-6">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-navy" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              </div>
              <h3 className="text-xl md:text-2xl font-bold text-medin-navy mb-3 md:mb-4">Visión</h3>
              <p className="text-sm md:text-base text-gray-600 leading-relaxed">
                Ser el sistema de gestión hospitalaria más accesible y confiable del mercado, transformando la experiencia de atención médica.
              </p>
            </div>

            {/* Valores */}
            <div className="bg-white rounded-lg shadow-md p-6 md:p-8 hover:shadow-lg transition-shadow">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-medin-cyan bg-opacity-10 rounded-lg flex items-center justify-center mb-4 md:mb-6">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-xl md:text-2xl font-bold text-medin-navy mb-3 md:mb-4">Valores</h3>
              <p className="text-sm md:text-base text-gray-600 leading-relaxed">
                Integridad, innovación, seguridad y compromiso con la excelencia en el cuidado de la salud.
              </p>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default Nosotros;
