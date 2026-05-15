/**
 * Componente Navbar - MedFlow Hospital System
 * Barra de navegación principal con información de contacto
 */

import { useState, useRef, useEffect } from 'react';
import type { FC } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import logo from '/icono.svg';
import { useAuth } from '../../hooks/useAuth';

const Navbar: FC = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const userMenuRef = useRef<HTMLDivElement>(null);
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const hideNav = pathname === '/login' || pathname === '/register';
  const { isAuthenticated, user, logout } = useAuth();

  const isPatient = isAuthenticated && user?.roles?.includes('PATIENT');

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (userMenuRef.current && !userMenuRef.current.contains(e.target as Node)) {
        setIsUserMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setIsUserMenuOpen(false);
    navigate('/');
  };

  const displayName = user?.fullName?.split(' ')[0] ?? user?.username ?? 'Mi cuenta';

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
                    to="/appointment"
                    className="hidden md:inline-flex px-3 md:px-4 lg:px-6 py-2 bg-medin-navy border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-xs md:text-sm lg:text-base"
                  >
                    Agendar Cita
                  </Link>
                  {/* User dropdown */}
                  <div className="relative" ref={userMenuRef}>
                    <button
                      onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                      className="flex items-center gap-2 px-3 md:px-4 py-2 bg-medin-cyan text-medin-navy rounded-full font-semibold hover:bg-cyan-300 transition-colors text-xs md:text-sm lg:text-base"
                    >
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <span className="hidden sm:inline">{displayName}</span>
                      <svg className={`w-3 h-3 transition-transform ${isUserMenuOpen ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                      </svg>
                    </button>
                    {isUserMenuOpen && (
                      <div className="absolute left-0 mt-3 w-48 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-50">
                        <div className="absolute -top-1.5 left-5 w-3 h-3 bg-white border-l border-t border-gray-100 rotate-45" />
                        <Link
                          to="/patient/profile"
                          onClick={() => setIsUserMenuOpen(false)}
                          className="flex items-center gap-3 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                          <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                          </svg>
                          Mi Perfil
                        </Link>
                        <Link
                          to="/patient"
                          onClick={() => setIsUserMenuOpen(false)}
                          className="flex items-center gap-3 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                          <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                          Mis Citas
                        </Link>
                        <hr className="my-1 border-gray-100" />
                        <button
                          onClick={handleLogout}
                          className="flex items-center gap-3 px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition-colors w-full text-left"
                        >
                          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                          </svg>
                          Salir
                        </button>
                      </div>
                    )}
                  </div>
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
                      <Link to="/appointment" className="block text-center px-4 py-2 border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-sm" onClick={() => setIsMobileMenuOpen(false)}>
                        Agendar Cita
                      </Link>
                      <Link to="/patient/profile" className="block text-center text-white hover:text-medin-cyan transition-colors text-sm font-medium py-2" onClick={() => setIsMobileMenuOpen(false)}>
                        Mi Perfil
                      </Link>
                      <Link to="/patient" className="block text-center text-white hover:text-medin-cyan transition-colors text-sm font-medium py-2" onClick={() => setIsMobileMenuOpen(false)}>
                        Mis Citas
                      </Link>
                      <button onClick={() => { handleLogout(); setIsMobileMenuOpen(false); }} className="block w-full text-center text-red-400 hover:text-red-300 transition-colors text-sm font-medium py-2">
                        Salir
                      </button>
                    </>
                  ) : (
                    <>
                      <Link to="/login" className="block text-center px-4 py-2 border-2 border-medin-cyan text-medin-cyan rounded-full font-semibold hover:bg-medin-cyan hover:text-medin-navy transition-colors text-sm" onClick={() => setIsMobileMenuOpen(false)}>
                        Iniciar Sesión
                      </Link>
                      <Link to="/register" className="block text-center px-4 py-2 border-2 border-white text-white rounded-full font-semibold hover:bg-white hover:text-medin-navy transition-colors text-sm" onClick={() => setIsMobileMenuOpen(false)}>
                        Registrarse
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
