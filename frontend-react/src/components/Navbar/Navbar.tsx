/**
 * Componente Navbar - MedFlow Hospital System
 * Barra de navegación principal con información de contacto
 */

import { useState } from 'react';
import type { FC } from 'react';
import { Link } from 'react-router-dom';
import logo from '/icono.svg';

const Navbar: FC = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  return (
    <header className="w-full">
      {/* Top Info Bar */}
      <div className="bg-gray-100 py-3 px-4 md:px-6">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          {/* Logo MedFlow con icono */}
          <Link to="/" className="flex items-center gap-2 hover:opacity-80 transition-opacity">
            <img src={logo} alt="MedFlow Logo" className="h-8 md:h-12 w-auto" />
            <span className="text-xl md:text-3xl font-bold">
              <span className="text-medin-navy">Med</span>
              <span className="text-medin-cyan">Flow</span>
            </span>
          </Link>

          {/* Contact Info - Hidden on mobile */}
          <div className="hidden lg:flex items-center gap-4 xl:gap-8">
            {/* Emergency */}
            <div className="flex items-center gap-2">
              <svg className="w-6 h-6 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
              <div className="text-sm">
                <div className="text-gray-600 font-medium">EMERGENCIA</div>
                <div className="text-medin-cyan font-semibold">(+502) 1122-3344</div>
              </div>
            </div>

            {/* Work Hour */}
            <div className="flex items-center gap-2">
              <svg className="w-6 h-6 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div className="text-sm">
                <div className="text-gray-600 font-medium">HORA DE TRABAJO</div>
                <div className="text-medin-cyan font-semibold">09:00 - 20:00 Todos los días</div>
              </div>
            </div>

            {/* Location */}
            <div className="flex items-center gap-2">
              <svg className="w-6 h-6 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <div className="text-sm">
                <div className="text-gray-600 font-medium">Ubicación</div>
                <div className="text-medin-cyan font-semibold">0123 Algun lugar</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Navigation */}
      <nav className="bg-medin-navy py-4 px-4 md:px-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          {/* Navigation Links - Desktop */}
          <ul className="hidden md:flex items-center gap-4 lg:gap-8">
            <li>
              <Link to="/" className="text-white hover:text-medin-cyan transition-colors font-medium">
                Home
              </Link>
            </li>
            <li>
              <Link to="/about" className="text-white hover:text-medin-cyan transition-colors font-medium">
                About us
              </Link>
            </li>
            <li>
              <Link to="/services" className="text-white hover:text-medin-cyan transition-colors font-medium">
                Services
              </Link>
            </li>
            <li>
              <Link to="/news" className="text-white hover:text-medin-cyan transition-colors font-medium">
                News
              </Link>
            </li>
            <li>
              <Link to="/contact" className="text-white hover:text-medin-cyan transition-colors font-medium">
                Contact
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

          {/* Action Buttons - Both Mobile and Desktop */}
          <div className="flex items-center gap-2 md:gap-4">
            {/* Login Button */}
            <Link
              to="/login"
              className="px-3 md:px-4 lg:px-6 py-2 bg-medin-navy border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-xs md:text-sm lg:text-base"
            >
              Iniciar Sesión
            </Link>
            
            {/* Appointment Button */}
            <Link
              to="/appointment"
              className="px-3 md:px-4 lg:px-6 py-2 bg-medin-blue text-medin-navy rounded-full font-semibold hover:bg-medin-blue-light transition-colors text-xs md:text-sm lg:text-base"
            >
              Agendar Cita
            </Link>
          </div>
        </div>

        {/* Mobile Menu Dropdown */}
        {isMobileMenuOpen && (
          <div className="md:hidden mt-4 pb-4">
            <ul className="flex flex-col space-y-3">
              <li>
                <Link
                  to="/"
                  className="block text-white hover:text-medin-cyan transition-colors font-medium py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  Home
                </Link>
              </li>
              <li>
                <Link
                  to="/about"
                  className="block text-white hover:text-medin-cyan transition-colors font-medium py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  About us
                </Link>
              </li>
              <li>
                <Link
                  to="/services"
                  className="block text-white hover:text-medin-cyan transition-colors font-medium py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  Services
                </Link>
              </li>
              <li>
                <Link
                  to="/doctors"
                  className="block text-white hover:text-medin-cyan transition-colors font-medium py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  Doctors
                </Link>
              </li>
              <li>
                <Link
                  to="/news"
                  className="block text-white hover:text-medin-cyan transition-colors font-medium py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  News
                </Link>
              </li>
              <li>
                <Link
                  to="/contact"
                  className="block text-white hover:text-medin-cyan transition-colors font-medium py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  Contact
                </Link>
              </li>
            </ul>
          </div>
        )}
      </nav>
    </header>
  );
};

export default Navbar;
