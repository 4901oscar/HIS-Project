package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.LabOrder;

public interface LabServiceClient {
    void notifyNewLabOrder(LabOrder labOrder, String appointmentId);
    void completeOrderByAppointmentId(String appointmentId);
}
