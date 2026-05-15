/**
 * MainLayout - Layout principal con Sidebar para módulos según rol
 */

import { useState, useRef, useEffect } from 'react';
import type { FC, ReactNode } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  CalendarIcon,
  UserGroupIcon,
  BeakerIcon,
  BuildingStorefrontIcon,
  BanknotesIcon,
  Cog6ToothIcon,
  ArrowLeftOnRectangleIcon,
  Bars3Icon,
  XMarkIcon,
  ClipboardDocumentCheckIcon,
  UserCircleIcon,
  EyeIcon,
  EyeSlashIcon,
} from '@heroicons/react/24/outline';
import HistorialFloatingButton from '../shared/HistorialFloatingButton';
import { changePassword } from '../../services/authService';

interface MenuItem {
  name: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  roles: string[];
}

const menuItems: MenuItem[] = [
  {
    name: 'Administración',
    path: '/administrator',
    icon: Cog6ToothIcon,
    roles: ['ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Admisión',
    path: '/admission',
    icon: CalendarIcon,
    roles: ['ADMISSION', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Triaje Pendiente',
    path: '/vitals/triage',
    icon: ClipboardDocumentCheckIcon,
    roles: ['VITAL_SIGNS'],
  },
  {
    name: 'Consultas',
    path: '/doctor/consultas',
    icon: UserGroupIcon,
    roles: ['DOCTOR', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Laboratorio',
    path: '/lab',
    icon: BeakerIcon,
    roles: ['LABORATORY', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Farmacia',
    path: '/pharmacy',
    icon: BuildingStorefrontIcon,
    roles: ['PHARMACY', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Caja',
    path: '/cashier',
    icon: BanknotesIcon,
    roles: ['CASHIER', 'ADMINISTRATOR', 'ADMIN'],
  },
];

interface MainLayoutProps {
  children: ReactNode;
}

const inputCls = (error?: string) =>
  `w-full px-3 py-2.5 border ${error ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm`;

const MainLayout: FC<MainLayoutProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const [profileOpen, setProfileOpen] = useState(false);
  const [pwForm, setPwForm] = useState({ current: '', next: '', confirm: '' });
  const [pwErrors, setPwErrors] = useState<Record<string, string>>({});
  const [pwSaving, setPwSaving] = useState(false);
  const [pwSuccess, setPwSuccess] = useState(false);
  const [pwError, setPwError] = useState<string | null>(null);
  const [showPw, setShowPw] = useState({ current: false, next: false, confirm: false });
  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!profileOpen) return;
    const handler = (e: MouseEvent) => {
      if (modalRef.current && !modalRef.current.contains(e.target as Node)) setProfileOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [profileOpen]);

  const handlePasswordSave = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors: Record<string, string> = {};
    if (!pwForm.current.trim()) errors.current = 'Requerido';
    if (pwForm.next.length < 8) errors.next = 'Mínimo 8 caracteres';
    if (pwForm.next !== pwForm.confirm) errors.confirm = 'No coinciden';
    if (Object.keys(errors).length > 0) { setPwErrors(errors); return; }
    setPwErrors({});
    setPwSaving(true);
    setPwError(null);
    try {
      await changePassword(pwForm.current, pwForm.next);
      setPwSuccess(true);
      setPwForm({ current: '', next: '', confirm: '' });
      setTimeout(() => setPwSuccess(false), 3000);
    } catch {
      setPwError('No se pudo cambiar la contraseña. Verifica tu contraseña actual.');
    } finally {
      setPwSaving(false);
    }
  };

  const userRole = user?.roles?.[0] || 'ADMISSION';
  const userName = user?.fullName || 'Personal';

  const filteredMenu = menuItems.filter((item) =>
    item.roles.includes(userRole)
  );

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const currentPageName =
    filteredMenu.find((item) => location.pathname.startsWith(item.path))?.name ?? 'MedFlow HIS';

  const today = new Date().toLocaleDateString('es-GT', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Mobile sidebar backdrop */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-gray-600 bg-opacity-75 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-medin-navy transform transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        aria-label="Navegación principal"
      >
        {/* Logo */}
        <div className="flex items-center justify-between h-16 px-6 bg-medin-navy border-b border-medin-cyan/20">
          <div className="flex items-center space-x-2">
            <img src="/icono.svg" alt="Logo MedFlow" className="h-8 w-auto" />
            <span className="text-xl font-bold">
              <span className="text-white">Med</span>
              <span className="text-medin-cyan">Flow</span>
            </span>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden text-gray-400 hover:text-white"
            aria-label="Cerrar menú"
          >
            <XMarkIcon className="h-6 w-6" aria-hidden="true" />
          </button>
        </div>

        {/* User Info */}
        <div className="px-6 py-4 border-b border-medin-cyan/20">
          <div className="flex items-center space-x-3">
            <div
              className="h-10 w-10 rounded-full bg-medin-cyan flex items-center justify-center text-medin-navy font-bold"
              aria-hidden="true"
            >
              {userName.charAt(0).toUpperCase()}
            </div>
            <div>
              <p className="text-white font-medium text-sm">{userName}</p>
              <p className="text-gray-400 text-xs">{userRole}</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="px-3 py-4 space-y-1" aria-label="Módulos del sistema">
          {filteredMenu.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname.startsWith(item.path);

            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                aria-current={isActive ? 'page' : undefined}
                className={`flex items-center space-x-3 px-3 py-2.5 rounded-lg transition-colors ${
                  isActive
                    ? 'bg-medin-cyan text-medin-navy font-medium'
                    : 'text-gray-300 hover:bg-medin-navy/50 hover:text-white'
                }`}
              >
                <Icon className="h-5 w-5" aria-hidden="true" />
                <span className="text-sm">{item.name}</span>
              </Link>
            );
          })}
        </nav>

        {/* Logout Button */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-medin-cyan/20">
          <button
            onClick={handleLogout}
            className="flex items-center space-x-3 px-3 py-2.5 rounded-lg text-gray-300 hover:bg-red-600/20 hover:text-red-400 transition-colors w-full"
            aria-label="Cerrar sesión"
          >
            <ArrowLeftOnRectangleIcon className="h-5 w-5" aria-hidden="true" />
            <span className="text-sm">Cerrar sesión</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="lg:pl-64">
        {/* Top bar */}
        <header className="bg-white shadow-sm h-16 flex items-center justify-between px-4 lg:px-8">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden text-gray-600 hover:text-gray-900"
            aria-label="Abrir menú"
          >
            <Bars3Icon className="h-6 w-6" aria-hidden="true" />
          </button>

          <div className="flex-1 lg:flex-none">
            <h1 className="text-lg font-semibold text-gray-900">{currentPageName}</h1>
          </div>

          <div className="flex items-center space-x-3">
            <span className="hidden sm:block text-sm text-gray-600 capitalize">{today}</span>
            <button
              onClick={() => { setProfileOpen(true); setPwSuccess(false); setPwError(null); setPwErrors({}); }}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-medin-navy transition-colors text-sm font-medium"
              title="Mi perfil"
            >
              <UserCircleIcon className="h-5 w-5" />
              <span className="hidden md:inline"></span>
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="p-4 lg:p-8">{children}</main>
      </div>

      <HistorialFloatingButton />

      {profileOpen && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
          <div ref={modalRef} className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6">
            <div className="flex items-center justify-between mb-5">
              <h2 className="text-base font-semibold text-gray-900 flex items-center gap-2">
                <UserCircleIcon className="h-5 w-5 text-medin-cyan" />
                Mi perfil
              </h2>
              <button onClick={() => setProfileOpen(false)} className="text-gray-400 hover:text-gray-600">
                <XMarkIcon className="h-5 w-5" />
              </button>
            </div>

            <div className="mb-5 p-3 bg-gray-50 rounded-lg">
              <p className="text-xs text-gray-500 mb-0.5">Usuario</p>
              <p className="text-sm font-medium text-gray-800">{userName}</p>
              <p className="text-xs text-gray-400 mt-0.5">{user?.email}</p>
            </div>

            <h3 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
              <svg className="w-4 h-4 text-medin-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
              Cambiar contraseña
            </h3>

            {pwError && <div className="mb-3 p-2.5 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs">{pwError}</div>}
            {pwSuccess && <div className="mb-3 p-2.5 bg-green-50 border border-green-200 rounded-lg text-green-700 text-xs">Contraseña actualizada correctamente.</div>}

            <form onSubmit={handlePasswordSave} className="space-y-3">
              {(['current', 'next', 'confirm'] as const).map((field) => {
                const labels = { current: 'Contraseña actual', next: 'Nueva contraseña', confirm: 'Confirmar nueva' };
                return (
                  <div key={field}>
                    <label className="block text-xs font-medium text-gray-600 mb-1">{labels[field]}</label>
                    <div className="relative">
                      <input
                        type={showPw[field] ? 'text' : 'password'}
                        value={pwForm[field]}
                        onChange={e => { setPwForm(f => ({ ...f, [field]: e.target.value })); setPwErrors(er => ({ ...er, [field]: '' })); }}
                        className={`${inputCls(pwErrors[field])} pr-10`}
                      />
                      <button type="button" onClick={() => setShowPw(s => ({ ...s, [field]: !s[field] }))}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600">
                        {showPw[field] ? <EyeSlashIcon className="h-4 w-4" /> : <EyeIcon className="h-4 w-4" />}
                      </button>
                    </div>
                    {pwErrors[field] && <p className="mt-1 text-xs text-red-500">{pwErrors[field]}</p>}
                  </div>
                );
              })}
              <p className="text-xs text-gray-400">Mínimo 8 caracteres.</p>
              <button type="submit" disabled={pwSaving}
                className="w-full py-2.5 bg-medin-navy text-white text-sm font-semibold rounded-lg hover:bg-medin-blue transition-colors disabled:opacity-50 mt-1">
                {pwSaving ? 'Actualizando...' : 'Cambiar contraseña'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default MainLayout;
