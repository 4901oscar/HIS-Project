/**
 * BookAppointmentForm - Formulario para agendar citas
 */

import { useState } from 'react';
import type { ChangeEvent, FormEvent, FC } from 'react';
import type { AppointmentFormData } from '../../types/appointment';

interface BookAppointmentFormProps {
  onSuccess?: (message: string) => void;
  onError?: (message: string) => void;
}

const BookAppointmentForm: FC<BookAppointmentFormProps> = ({ onSuccess, onError }) => {
  const [formData, setFormData] = useState<AppointmentFormData>({
    dpi: '' as unknown as number, // Inicialmente vacío, se convertirá a número
    nit: '' as unknown as number, // Inicialmente vacío, se convertirá a número
    name: '',
    gender: '',
    email: '',
    phone: '',
    date: '',
    time: '',
    message: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Limpiar error cuando el usuario empieza a escribir
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.dpi) newErrors.dpi = 'DPI is required';
    if (!formData.nit) newErrors.nit = 'NIT is required';
    if (!formData.name.trim()) newErrors.name = 'Name is required';
    if (!formData.gender) newErrors.gender = 'Gender is required';
    if (!formData.email.trim()) newErrors.email = 'Email is required';
    if (!formData.phone.trim()) newErrors.phone = 'Phone is required';
    if (!formData.date) newErrors.date = 'Date is required';
    if (!formData.time) newErrors.time = 'Time is required';
    

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) {
      onError?.('Please fill all required fields');
      return;
    }

    setIsLoading(true);

    try {
      // Simulación - aquí iría la llamada al servicio
      await new Promise((resolve) => setTimeout(resolve, 1500));
      
      onSuccess?.('Appointment booked successfully!');
      
      // Resetear formulario
      setFormData({
        dpi: 0,
        nit: 0,
        name: '',
        gender: '',
        email: '',
        phone: '',
        date: '',
        time: '',
        message: '',
      });
    } catch {
      onError?.('Error booking appointment');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* DPI and NIT Row */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <input
            type="number"
            name="dpi"
            value={formData.dpi}
            onChange={handleChange}
            placeholder="DPI"
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.dpi ? 'ring-2 ring-red-500' : ''
            }`}
          />
        </div>
        <div>
          <input
            type="number"
            name="nit"
            value={formData.nit}
            onChange={handleChange}
            placeholder="NIT"
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.nit ? 'ring-2 ring-red-500' : ''
            }`}
          />
        </div>
      </div>

      {/* Name and Gender Row */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            placeholder="Name"
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.name ? 'ring-2 ring-red-500' : ''
            }`}
          />
        </div>
        <div>
          <select
            name="gender"
            value={formData.gender}
            onChange={handleChange}
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.gender ? 'ring-2 ring-red-500' : ''
            } ${!formData.gender ? 'text-gray-400' : ''}`}
          >
            <option value="">Gender</option>
            <option value="male">Male</option>
            <option value="female">Female</option>
            <option value="other">Other</option>
          </select>
        </div>
      </div>

      {/* Email and Phone Row */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="Email"
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.email ? 'ring-2 ring-red-500' : ''
            }`}
          />
        </div>
        <div>
          <input
            type="tel"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            placeholder="Phone"
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.phone ? 'ring-2 ring-red-500' : ''
            }`}
          />
        </div>
      </div>

      {/* Date and Time Row */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <select
            name="date"
            value={formData.date}
            onChange={handleChange}
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.date ? 'ring-2 ring-red-500' : ''
            } ${!formData.date ? 'text-gray-400' : ''}`}
          >
            <option value="">Date</option>
            <option value="2026-03-06">March 6, 2026</option>
            <option value="2026-03-07">March 7, 2026</option>
            <option value="2026-03-08">March 8, 2026</option>
            <option value="2026-03-09">March 9, 2026</option>
            <option value="2026-03-10">March 10, 2026</option>
          </select>
        </div>
        <div>
          <select
            name="time"
            value={formData.time}
            onChange={handleChange}
            className={`w-full px-4 py-3 bg-medin-navy border-0 text-white focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
              errors.time ? 'ring-2 ring-red-500' : ''
            } ${!formData.time ? 'text-gray-400' : ''}`}
          >
            <option value="">Time</option>
            <option value="09:00">09:00 AM</option>
            <option value="10:00">10:00 AM</option>
            <option value="11:00">11:00 AM</option>
            <option value="14:00">02:00 PM</option>
            <option value="15:00">03:00 PM</option>
            <option value="16:00">04:00 PM</option>
          </select>
        </div>
      </div>

      

      {/* Message */}
      <div>
        <textarea
          name="message"
          value={formData.message}
          onChange={handleChange}
          placeholder="Message"
          rows={4}
          className="w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan resize-none"
        />
      </div>

      {/* Submit Button */}
      <button
        type="submit"
        disabled={isLoading}
        className="w-full py-3 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isLoading ? 'SUBMITTING...' : 'SUBMIT'}
      </button>
    </form>
  );
};

export default BookAppointmentForm;
