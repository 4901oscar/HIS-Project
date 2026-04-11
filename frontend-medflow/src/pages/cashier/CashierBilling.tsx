/**
 * CashierBilling - Caja y Facturación (SAT/FEL)
 */

import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';

const CashierBilling: FC = () => {
  return (
    <MainLayout>
      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-900">Cashier & Billing</h2>
        <p className="text-gray-600">Process payments and generate invoices (SAT/FEL)</p>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-gray-500">Billing and payment interface will be implemented here</p>
        </div>
      </div>
    </MainLayout>
  );
};

export default CashierBilling;
