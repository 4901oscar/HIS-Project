/**
 * Footer - MedFlow Hospital System
 */

import type { FC } from 'react';
import { Link } from 'react-router-dom';

const Footer: FC = () => {
  return (
    <footer>
      {/* Contact Cards Section */}
      <section className="py-12 md:py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <div className="text-center mb-8 md:mb-12">
            <p className="text-medin-cyan font-semibold text-xs md:text-sm uppercase tracking-wide mb-2">
              CONTACTA CON NOSOTROS
            </p>
            <h2 className="text-2xl md:text-4xl font-bold text-medin-navy">Contacto</h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6">
            {/* Emergency */}
            <div className="bg-medin-blue bg-opacity-30 rounded-lg p-6 md:p-8 text-center">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-3 md:mb-4">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
              </div>
              <h3 className="text-sm font-bold text-medin-navy mb-2 uppercase">EMERGENCIA</h3>
              <p className="text-medin-cyan font-semibold">(+502) 1234-5678</p>
            </div>

            {/* Location */}
            <div className="bg-medin-navy rounded-lg p-6 md:p-8 text-center text-white">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-white bg-opacity-10 rounded-full flex items-center justify-center mx-auto mb-3 md:mb-4">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
              <h3 className="text-sm font-bold mb-2 uppercase">UBICACIÓN</h3>
              <p className="text-sm">0123 algun lugar</p>
              <p className="text-sm">9876 Alguna ciudad</p>
            </div>

            {/* Email */}
            <div className="bg-medin-blue bg-opacity-30 rounded-lg p-6 md:p-8 text-center">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-3 md:mb-4">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <h3 className="text-sm font-bold text-medin-navy mb-2 uppercase">CORREO</h3>
              <p className="text-sm text-medin-navy">dev@gmail.com</p>
              <p className="text-sm text-medin-navy">medflow@gmail.com</p>
            </div>

            {/* Working Hours */}
            <div className="bg-medin-blue bg-opacity-30 rounded-lg p-6 md:p-8 text-center">
              <div className="w-12 h-12 md:w-16 md:h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-3 md:mb-4">
                <svg className="w-6 h-6 md:w-8 md:h-8 text-medin-cyan" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-sm font-bold text-medin-navy mb-2 uppercase">HORAS DE TRABAJO</h3>
              <p className="text-sm text-medin-navy">Lun-Sab 09:00-20:00</p>
              <p className="text-sm text-medin-navy">Domingos solo emergencias</p>
            </div>
          </div>
        </div>
      </section>

      {/* Main Footer */}
      <div className="bg-medin-navy text-white py-8 md:py-12">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 md:gap-8 mb-6 md:mb-8">
            {/* Brand */}
            <div>
              <div className="flex items-center mb-4">
                <span className="text-2xl md:text-3xl font-bold">
                  <span className="text-white">MED</span>
                  <span className="text-medin-cyan">Flow</span>
                </span>
              </div>
              <p className="text-gray-300 text-sm">
                Liderando la excelencia médica, atención de confianza
              </p>
            </div>

            {/* Important Links */}
            <div>
              <h3 className="font-bold mb-4">Important Links</h3>
              <ul className="space-y-2 text-sm text-gray-300">
                <li><Link to="/appointment" className="hover:text-medin-cyan transition-colors">Appointment</Link></li>
                <li><Link to="/doctors" className="hover:text-medin-cyan transition-colors">Doctors</Link></li>
                <li><Link to="/services" className="hover:text-medin-cyan transition-colors">Services</Link></li>
                <li><Link to="/about" className="hover:text-medin-cyan transition-colors">About Us</Link></li>
              </ul>
            </div>

            {/* Contact Us */}
            <div>
              <h3 className="font-bold mb-4">Contact Us</h3>
              <ul className="space-y-2 text-sm text-gray-300">
                <li>Call: (+254) 717 783 146</li>
                <li>Email: medflow@gmail.com</li>
                <li>Address: 0123 Some place</li>
                <li>Some country</li>
              </ul>
            </div>

            {/* Newsletter */}
            <div>
              <h3 className="font-bold mb-4">Newsletter</h3>
              <div className="flex">
                <input
                  type="email"
                  placeholder="Enter your email address"
                  className="flex-1 px-4 py-2 bg-white bg-opacity-10 border border-gray-600 text-white placeholder-gray-400 focus:outline-none focus:border-medin-cyan text-sm"
                />
                <button className="px-4 bg-medin-cyan hover:bg-medin-cyan-dark transition-colors">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          {/* Bottom Bar */}
          <div className="pt-6 md:pt-8 border-t border-gray-700 flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-xs md:text-sm text-gray-400 text-center md:text-left">
              © 2022 MedFlow All Rights Reserved by knhs
            </p>
            <div className="flex gap-3 md:gap-4">
              <a href="#" className="w-8 h-8 bg-white bg-opacity-10 rounded-full flex items-center justify-center hover:bg-medin-cyan transition-colors">
                <span className="sr-only">LinkedIn</span>
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
                </svg>
              </a>
              <a href="#" className="w-8 h-8 bg-white bg-opacity-10 rounded-full flex items-center justify-center hover:bg-medin-cyan transition-colors">
                <span className="sr-only">Facebook</span>
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                </svg>
              </a>
              <a href="#" className="w-8 h-8 bg-white bg-opacity-10 rounded-full flex items-center justify-center hover:bg-medin-cyan transition-colors">
                <span className="sr-only">Instagram</span>
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/>
                </svg>
              </a>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
