package com.medflow.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String SENDER_NAME = "MedFlow HIS";

    @Value("${BREVO_API_KEY:}")
    private String apiKey;

    @Value("${BREVO_SENDER_EMAIL:avilajoze123@gmail.com}")
    private String senderEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendActivationEmail(String toEmail, String firstName, String token) {
        String link = frontendUrl + "/activate?token=" + token;
        send(toEmail, firstName, "Activa tu cuenta en MedFlow HIS", buildActivationHtml(firstName, link));
    }

    @Async
    public void sendTempPasswordEmail(String toEmail, String firstName,
                                      String username, String tempPassword) {
        send(toEmail, firstName, "Bienvenido a MedFlow HIS - Credenciales de acceso",
                buildTempPasswordHtml(firstName, username, tempPassword, frontendUrl + "/login"));
    }

    @Async
    public void sendEmployeeCreatedEmail(String toEmail, String firstName,
                                         String username, String tempPassword) {
        send(toEmail, firstName, "Cuenta de empleado creada - MedFlow HIS",
                buildEmployeeHtml(firstName, username, tempPassword, frontendUrl + "/login"));
    }

    @Async
    public void sendAppointmentConfirmationEmail(String toEmail, String firstName,
                                                 String appointmentDate, String appointmentTime,
                                                 String doctorName, String invoiceNumber,
                                                 String qrCodeBase64, String notes,
                                                 String validFromTime, String validUntilTime) {
        send(toEmail, firstName, "Confirmacion de Cita - MedFlow HIS",
                buildAppointmentConfirmationHtml(firstName, appointmentDate, appointmentTime,
                        doctorName, invoiceNumber, qrCodeBase64, notes, validFromTime, validUntilTime));
    }

    private void send(String toEmail, String toName, String subject, String htmlBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> sender = Map.of("name", SENDER_NAME, "email", senderEmail);
            Map<String, Object> recipient = Map.of("email", toEmail, "name", toName);

            Map<String, Object> body = Map.of(
                    "sender", sender,
                    "to", List.of(recipient),
                    "subject", subject,
                    "htmlContent", htmlBody
            );

            restTemplate.postForEntity(BREVO_URL, new HttpEntity<>(body, headers), String.class);
            log.info("[Email] Enviado a {} - {}", toEmail, subject);
        } catch (Exception e) {
            log.error("[Email] Error al enviar a {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildActivationHtml(String firstName, String link) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;letter-spacing:1px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informacion Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Hola, " + firstName + "</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 24px;\">Gracias por registrarte en MedFlow HIS. Para activar tu cuenta haz clic en el boton:</p>"
            + "<div style=\"text-align:center;margin:0 0 28px;\">"
            + "<a href=\"" + link + "\" style=\"display:inline-block;background:#00d4e8;color:#0f4c75;font-weight:bold;font-size:15px;padding:14px 32px;border-radius:6px;text-decoration:none;\">Activar mi cuenta</a></div>"
            + "<p style=\"color:#888;font-size:12px;margin:0 0 8px;\">El enlace es valido por <strong>24 horas</strong>.</p>"
            + "<p style=\"color:#bbb;font-size:11px;margin:0;\">Si el boton no funciona copia este enlace:<br><span style=\"color:#00d4e8;\">" + link + "</span></p>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    private String buildTempPasswordHtml(String firstName, String username,
                                          String tempPassword, String loginUrl) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informacion Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Bienvenido/a, " + firstName + "</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 20px;\">Tu cuenta de paciente ha sido creada. Tus credenciales temporales:</p>"
            + "<table style=\"background:#f0faff;border:1px solid #b8e4f0;border-radius:6px;width:100%;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:14px 20px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Usuario (DPI):</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:16px;color:#0f4c75;font-weight:bold;\">" + username + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Contrasena temporal:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:20px;color:#0f4c75;font-weight:bold;letter-spacing:2px;\">" + tempPassword + "</p></td></tr></table>"
            + "<p style=\"color:#e74c3c;font-size:13px;margin:0 0 24px;\">Cambia tu contrasena en el primer inicio de sesion.</p>"
            + "<div style=\"text-align:center;\"><a href=\"" + loginUrl + "\" style=\"display:inline-block;background:#00d4e8;color:#0f4c75;font-weight:bold;font-size:15px;padding:14px 32px;border-radius:6px;text-decoration:none;\">Iniciar sesion</a></div>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    private String buildEmployeeHtml(String firstName, String username,
                                      String tempPassword, String loginUrl) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informacion Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Cuenta creada, " + firstName + "</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 20px;\">El administrador ha creado tu cuenta de empleado en MedFlow HIS:</p>"
            + "<table style=\"background:#f0faff;border:1px solid #b8e4f0;border-radius:6px;width:100%;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:14px 20px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Usuario:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:16px;color:#0f4c75;font-weight:bold;\">" + username + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Contrasena temporal:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:20px;color:#0f4c75;font-weight:bold;letter-spacing:2px;\">" + tempPassword + "</p></td></tr></table>"
            + "<p style=\"color:#e74c3c;font-size:13px;margin:0 0 24px;\">Cambia tu contrasena en tu primer inicio de sesion.</p>"
            + "<div style=\"text-align:center;\"><a href=\"" + loginUrl + "\" style=\"display:inline-block;background:#00d4e8;color:#0f4c75;font-weight:bold;font-size:15px;padding:14px 32px;border-radius:6px;text-decoration:none;\">Acceder al sistema</a></div>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    private String buildAppointmentConfirmationHtml(String firstName, String appointmentDate,
                                                    String appointmentTime, String doctorName,
                                                    String invoiceNumber, String qrCodeBase64,
                                                    String notes, String validFromTime,
                                                    String validUntilTime) {
        String notesSection = (notes != null && !notes.isBlank())
            ? "<p style=\"color:#444;line-height:1.6;margin:16px 0 0;\"><strong>Notas:</strong> " + notes + "</p>"
            : "";

        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head>"
            + "<body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\">"
            + "<tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informacion Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Cita confirmada, " + firstName + "!</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 24px;\">Tu cita ha sido agendada exitosamente:</p>"
            + "<table style=\"background:#f0faff;border:1px solid #b8e4f0;border-radius:6px;width:100%;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:14px 20px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Fecha:</strong></p>"
            + "<p style=\"margin:0;font-size:16px;color:#0f4c75;font-weight:bold;\">" + appointmentDate + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Hora:</strong></p>"
            + "<p style=\"margin:0;font-size:16px;color:#0f4c75;font-weight:bold;\">" + appointmentTime + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Doctor:</strong></p>"
            + "<p style=\"margin:0;font-size:16px;color:#0f4c75;font-weight:bold;\">" + doctorName + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Factura:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:14px;color:#0f4c75;font-weight:bold;\">" + invoiceNumber + "</p></td></tr>"
            + "</table>"
            + notesSection
            + "<div style=\"text-align:center;margin:24px 0;\">"
            + "<p style=\"color:#555;font-size:14px;margin:0 0 12px;\"><strong>Tu codigo QR de confirmacion:</strong></p>"
            + "<img src=\"data:image/png;base64," + qrCodeBase64 + "\" alt=\"QR Code\" style=\"width:200px;height:200px;border:2px solid #00d4e8;border-radius:8px;\"/>"
            + "<p style=\"color:#e74c3c;font-size:13px;margin:12px 0 0;\">QR valido desde <strong>" + validFromTime + "</strong> hasta <strong>" + validUntilTime + "</strong></p>"
            + "<p style=\"color:#888;font-size:12px;margin:8px 0 0;\">Presenta este codigo en recepcion el dia de tu cita</p>"
            + "</div>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\">"
            + "<p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS</p>"
            + "</td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    @Async
    public void sendTriageAlertEmail(String toEmail, String doctorName,
                                     String patientId, String appointmentId,
                                     String priorityLevel, String priorityDescription,
                                     int maxWaitMinutes) {
        String subject = "URGENTE - Paciente con triaje " + priorityDescription + " - MedFlow HIS";
        send(toEmail, doctorName, subject,
                buildTriageAlertHtml(doctorName, patientId, appointmentId,
                        priorityLevel, priorityDescription, maxWaitMinutes));
    }

    @Async
    public void sendLabResultsReadyEmail(String toEmail, String doctorName,
                                         String patientId, String appointmentId,
                                         String orderId) {
        send(toEmail, doctorName, "Resultados de laboratorio disponibles - MedFlow HIS",
                buildLabResultsReadyHtml(doctorName, patientId, appointmentId, orderId));
    }

    private String buildTriageAlertHtml(String doctorName, String patientId, String appointmentId,
                                        String priorityLevel, String priorityDescription, int maxWaitMinutes) {
        String badgeColor = "RED".equals(priorityLevel) ? "#c0392b" : "#e67e22";
        String waitText = maxWaitMinutes == 0 ? "Atención INMEDIATA" : "Máximo " + maxWaitMinutes + " minutos";
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head>"
            + "<body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informacion Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 8px;color:#1a1a2e;\">Dr. " + doctorName + "</h2>"
            + "<p style=\"color:#444;margin:0 0 24px;\">Se le asigno un paciente con prioridad de triaje alta.</p>"
            + "<div style=\"background:" + badgeColor + ";color:#fff;padding:12px 20px;border-radius:6px;margin:0 0 20px;text-align:center;\">"
            + "<strong style=\"font-size:18px;\">" + priorityDescription.toUpperCase() + " (" + priorityLevel + ")</strong><br>"
            + "<span style=\"font-size:13px;\">" + waitText + "</span></div>"
            + "<table style=\"width:100%;border-collapse:collapse;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:8px 12px;background:#f0f4f8;color:#555;font-size:13px;\"><strong>Cita:</strong></td>"
            + "<td style=\"padding:8px 12px;font-family:monospace;font-size:13px;\">" + appointmentId + "</td></tr>"
            + "<tr><td style=\"padding:8px 12px;color:#555;font-size:13px;\"><strong>Paciente:</strong></td>"
            + "<td style=\"padding:8px 12px;font-family:monospace;font-size:13px;\">" + patientId + "</td></tr>"
            + "</table>"
            + "<p style=\"color:#888;font-size:12px;margin:0;\">Ingrese al sistema para ver los detalles completos del paciente.</p>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    private String buildLabResultsReadyHtml(String doctorName, String patientId,
                                            String appointmentId, String orderId) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head>"
            + "<body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informacion Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 8px;color:#1a1a2e;\">Dr. " + doctorName + "</h2>"
            + "<p style=\"color:#444;margin:0 0 24px;\">Los resultados de laboratorio de su paciente ya estan disponibles en el sistema.</p>"
            + "<table style=\"width:100%;border-collapse:collapse;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:8px 12px;background:#f0f4f8;color:#555;font-size:13px;\"><strong>Orden:</strong></td>"
            + "<td style=\"padding:8px 12px;font-family:monospace;font-size:13px;\">" + orderId + "</td></tr>"
            + "<tr><td style=\"padding:8px 12px;color:#555;font-size:13px;\"><strong>Cita:</strong></td>"
            + "<td style=\"padding:8px 12px;font-family:monospace;font-size:13px;\">" + appointmentId + "</td></tr>"
            + "<tr><td style=\"padding:8px 12px;background:#f0f4f8;color:#555;font-size:13px;\"><strong>Paciente:</strong></td>"
            + "<td style=\"padding:8px 12px;font-family:monospace;font-size:13px;\">" + patientId + "</td></tr>"
            + "</table>"
            + "<p style=\"color:#888;font-size:12px;margin:0;\">Ingrese al sistema para revisar los resultados y continuar con la consulta.</p>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }
}
