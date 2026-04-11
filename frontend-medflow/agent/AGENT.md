# ⚛️ MedFlow Frontend - Agent Rules & Architecture

You are the Lead Frontend Engineer for the MedFlow HIS project. Your goal is to build a robust, role-based UI in React 18 that is ready for microservices integration.

## 🔑 Authentication & Access Control
- **Staff-Only Login:** ONLY hospital staff (Admissions, Doctors, Nurses, etc.) can authenticate.
- **Validation JWT:** This is the validation type, use the best practices of security for login.
- **No Patient Login:** Patients do not have accounts. They interact via public views (QR validation/results lookup).
- **Role-Based Routing:** Use Protected Routes to restrict access based on the following roles:
  - `ADMINISTRATOR`: Create profiles, Update information, Deactivate profiles
  - `ADMISSION`: Create, validate, and activate appointments.
  - `VITAL_SIGNS`: Capture patient vitals (Nurses).
  - `DOCTOR`: Clinical consultations and Triage Manchester.
  - `LABORATORY`: Manage samples and results.
  - `PHARMACY`: Dispense medications.
  - `CASHIER`: Billing and SAT (FEL) integration.

## 📁 Project Structure (src/)
- `/pages`: One folder per role (e.g., `/pages/admission`, `/pages/doctor`).
- `/components`: Reusable UI elements (Buttons, Tables, Modals).
- `/services`: THIS IS CRITICAL. All API calls must be abstracted here.
- `/hooks`: Custom hooks for session management and form handling.
- `/context`: Global state for the authenticated staff member.

## 🔌 API Connectivity Strategy (The "Placeholder" Rule)
To ensure the frontend is "ready to connect", follow these rules:
1. **Base URL:** Use `import.meta.env.VITE_API_GATEWAY_URL` as the root.
2. **Service Pattern:** Create files in `/services` (e.g., `patientService.js`).
3. **Mocking:** If the backend endpoint is not yet available, return a `Promise` with mock data, but keep the `axios` structure prepared and commented.

Example of a Service Placeholder:
```javascript
// src/services/appointmentService.js
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';

export const activateAppointment = async (id) => {
  // CLAUDE: Leave this ready for the Backend
  // return await axios.post(`${API_URL}/appointments/${id}/activate`);
  console.log(`Mock: Activating appointment ${id}`);
  return { status: 200, data: { message: "Activated" } };
};