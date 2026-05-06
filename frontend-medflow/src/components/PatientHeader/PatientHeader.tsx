import type { FC } from 'react';
import { ArrowLeftOnRectangleIcon, CalendarDaysIcon, UserCircleIcon } from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

interface PatientHeaderProps {
  onProfileClick?: () => void;
}

const PatientHeader: FC<PatientHeaderProps> = ({ onProfileClick }) => {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <header className="bg-medin-navy px-6 py-4 flex items-center justify-between shadow">
      <div className="flex items-center space-x-2 cursor-pointer" onClick={() => navigate('/patient')}>
        <img src="/icono.svg" alt="MedFlow" className="h-8 w-auto" />
        <span className="text-xl font-bold">
          <span className="text-white">Med</span>
          <span className="text-medin-cyan">Flow</span>
        </span>
      </div>
      <div className="flex items-center space-x-3">
        <button
          onClick={() => navigate('/appointment')}
          className="flex items-center space-x-1.5 bg-medin-cyan text-white px-3 py-1.5 rounded-lg text-sm font-semibold hover:bg-white transition-colors"
        >
          <CalendarDaysIcon className="h-4 w-4" />
          <span>Nueva Cita</span>
        </button>
        {onProfileClick ? (
          <button onClick={onProfileClick} className="text-gray-300 hover:text-white transition-colors" title="Mi perfil">
            <UserCircleIcon className="h-6 w-6" />
          </button>
        ) : (
          <button onClick={() => navigate('/patient', { state: { openProfile: true } })} className="text-gray-300 hover:text-white transition-colors" title="Mi perfil">
            <UserCircleIcon className="h-6 w-6" />
          </button>
        )}
        <button onClick={handleLogout} className="text-gray-300 hover:text-white transition-colors" title="Cerrar sesión">
          <ArrowLeftOnRectangleIcon className="h-6 w-6" />
        </button>
      </div>
    </header>
  );
};

export default PatientHeader;
