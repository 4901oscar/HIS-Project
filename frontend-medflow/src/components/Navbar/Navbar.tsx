/**
 * Componente Navbar - MedFlow Hospital System
 * Barra de navegación principal con información de contacto
 */

import { useState } from 'react';
import type { FC } from 'react';
import { Link, useLocation } from 'react-router-dom';
import logo from '/icono.svg';
import { useAuth } from '../../hooks/useAuth';

const Navbar: FC = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const { pathname } = useLocation();
  const hideNav = pathname === '/login' || pathname === '/register';
  const { isAuthenticated, user } = useAuth();

  const isPatient = isAuthenticated && user?.roles?.includes('PATIENT');

  return (
    <header className="w-full">
      {/* Main Navigation */}
      {!hideNav && (
        <nav className="bg-medin-navy py-3 px-4 md:px-6 lg:px-8">
          <div className="max-w-6xl lg:max-w-full mx-auto flex items-center justify-between gap-4 xl:px-40 2xl:px-60">
            {/* Logo MedFlow con icono */}
            <Link to="/" className="flex items-center gap-2 hover:opacity-80 transition-opacity flex-shrink-0">
              <img src={logo} alt="MedFlow Logo" className="h-8 md:h-10 w-auto" />
              <span className="text-lg md:text-2xl font-bold hidden sm:inline">
                <span className="text-white">Med</span>
                <span className="text-medin-cyan">Flow</span>
              </span>
            </Link>

            {/* Navigation Links - Desktop */}
            <ul className="hidden md:flex items-center gap-4 lg:gap-8 flex-1">
              <li>
                <Link to="/" className="text-white hover:text-medin-cyan transition-colors font-medium">
                  Inicio
                </Link>
              </li>
              <li>
                <Link to="/nosotros" className="text-white hover:text-medin-cyan transition-colors font-medium">
                  Nosotros
                </Link>
              </li>
              <li>
                <Link to="/servicios" className="text-white hover:text-medin-cyan transition-colors font-medium">
                  Servicios
                </Link>
              </li>
              
            </ul>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="md:hidden text-white hover:text-medin-cyan transition-colors"
              aria-label="Toggle menu"
            >
              {isMobileMenuOpen ? (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              ) : (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              )}
            </button>

            {/* Action Buttons */}
            <div className="flex items-center gap-2 md:gap-4">
              {isPatient ? (
                <>
                  <Link
                    to="/patient"
                    className="px-3 md:px-4 lg:px-6 py-2 bg-medin-navy border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-xs md:text-sm lg:text-base"
                  >
                    Ver Historial
                  </Link>
                  <Link
                    to="/appointment"
                    className="px-3 md:px-4 lg:px-6 py-2 bg-medin-blue text-medin-navy rounded-full font-semibold hover:bg-medin-blue-light transition-colors text-xs md:text-sm lg:text-base"
                  >
                    Agendar Cita
                  </Link>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="px-3 md:px-4 lg:px-6 py-2 bg-medin-navy border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-xs md:text-sm lg:text-base"
                  >
                    Iniciar Sesión
                  </Link>
                  <Link
                    to="/register"
                    className="px-3 md:px-4 lg:px-6 py-2 bg-medin-navy border-2 border-white text-white rounded-full font-semibold hover:bg-white hover:text-medin-navy transition-colors text-xs md:text-sm lg:text-base"
                  >
                    Registrarse
                  </Link>
      
                </>
              )}
            </div>
          </div>

          {/* Mobile Menu Dropdown */}
          {isMobileMenuOpen && (
            <div className="md:hidden mt-4 pb-4">
              <ul className="flex flex-col space-y-3">
                <li>
                  <Link to="/" className="block text-white hover:text-medin-cyan transition-colors font-medium py-2" onClick={() => setIsMobileMenuOpen(false)}>
                    Inicio
                  </Link>
                </li>
                <li>
                  <Link to="/nosotros" className="block text-white hover:text-medin-cyan transition-colors font-medium py-2" onClick={() => setIsMobileMenuOpen(false)}>
                    Nosotros
                  </Link>
                </li>
                <li>
                  <Link to="/servicios" className="block text-white hover:text-medin-cyan transition-colors font-medium py-2" onClick={() => setIsMobileMenuOpen(false)}>
                    Servicios
                  </Link>
                </li>
                
                <li className="pt-2 border-t border-white/20 flex flex-col gap-2">
                  {isPatient ? (
                    <>
                      <Link to="/patient" className="block text-center px-4 py-2 border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-sm" onClick={() => setIsMobileMenuOpen(false)}>
                        Ver Historial
                      </Link>
                      <Link to="/appointment" className="block text-center px-4 py-2 bg-medin-blue text-medin-navy rounded-full font-semibold transition-colors text-sm" onClick={() => setIsMobileMenuOpen(false)}>
                        Agendar Cita
                      </Link>
                    </>
                  ) : (
                    <>
                      <Link to="/login" className="block text-center px-4 py-2 border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-sm" onClick={() => setIsMobileMenuOpen(false)}>
                        Iniciar Sesión
                      </Link>
                      <Link to="/register" className="block text-center px-4 py-2 border-2 border-white text-white rounded-full font-semibold hover:bg-white hover:text-medin-navy transition-colors text-sm" onClick={() => setIsMobileMenuOpen(false)}>
                        Registrar
                      </Link>
                    </>
                  )}
                </li>
              </ul>
            </div>
          )}
        </nav>
      )}
    </header>
  );
};

export default Navbar;
