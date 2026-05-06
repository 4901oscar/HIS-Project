package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.Prescription;

public interface PharmacyServiceClient {
    void notifyNewPrescription(Prescription prescription);
}
