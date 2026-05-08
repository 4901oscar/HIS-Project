package com.medframe.auth.service;

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

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final String FROM = "MedFlow HIS <onboarding@resend.dev>";

    @Value("${RESEND_API_KEY:}")
    private String apiKey;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendActivationEmail(String toEmail, String firstName, String token) {
        String link = frontendUrl + "/activate?token=" + token;
        send(toEmail, "Activa tu cuenta en MedFlow HIS", buildActivationHtml(firstName, link));
    }

    @Async
    public void sendTempPasswordEmail(String toEmail, String firstName,
                                      String username, String tempPassword) {
        send(toEmail, "Bienvenido a MedFlow HIS - Credenciales de acceso",
                buildTempPasswordHtml(firstName, username, tempPassword, frontendUrl + "/login"));
    }

    @Async
    public void sendEmployeeCreatedEmail(String toEmail, String firstName,
                                         String username, String tempPassword) {
        send(toEmail, "Cuenta de empleado creada - MedFlow HIS",
                buildEmployeeHtml(firstName, username, tempPassword, frontendUrl + "/login"));
    }

    @Async
    public void sendAppointmentConfirmationEmail(String toEmail, String firstName,
                                                 String appointmentDate, String appointmentTime,
                                                 String doctorName, String invoiceNumber,
                                                 String qrCodeBase64, String notes,
                                                 String validFromTime, String validUntilTime) {
        send(toEmail, "Confirmacion de Cita - MedFlow HIS",
                buildAppointmentConfirmationHtml(firstName, appointmentDate, appointmentTime,
                        doctorName, invoiceNumber, qrCodeBase64, notes, validFromTime, validUntilTime));
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "from", FROM,
                    "to", List.of(to),
                    "subject", subject,
                    "html", htmlBody
            );

            restTemplate.postForEntity(RESEND_URL, new HttpEntity<>(body, headers), String.class);
            log.info("[Email] Enviado a {} - {}", to, subject);
        } catch (Exception e) {
            log.error("[Email] Error al enviar a {}: {}", to, e.getMessage());
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
            + "<p style=\"color:#888;font-size:12px;margin:0 0 8px;\">El enlace es valido por <strong>24 horas</strong>. Si no creaste esta cuenta, ignora este correo.</p>"
            + "<p style=\"color:#bbb;font-size:11px;margin:0;\">Si el boton no funciona copia este enlace:<br><span style=\"color:#00d4e8;\">" + link + "</span></p>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS &middot; Correo automatico.</p></td></tr>"
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
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 20px;\">Tu cuenta de paciente ha sido creada. Tus credenciales de acceso temporales:</p>"
            + "<table style=\"background:#f0faff;border:1px solid #b8e4f0;border-radius:6px;width:100%;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:14px 20px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Usuario (DPI):</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:16px;color:#0f4c75;font-weight:bold;\">" + username + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Contrasena temporal:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:20px;color:#0f4c75;font-weight:bold;letter-spacing:2px;\">" + tempPassword + "</p></td></tr></table>"
            + "<p style=\"color:#e74c3c;font-size:13px;margin:0 0 24px;\">Cambia tu contrasena en el primer inicio de sesion.</p>"
            + "<div style=\"text-align:center;\"><a href=\"" + loginUrl + "\" style=\"display:inline-block;background:#00d4e8;color:#0f4c75;font-weight:bold;font-size:15px;padding:14px 32px;border-radius:6px;text-decoration:none;\">Iniciar sesion</a></div>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS &middot; Correo automatico.</p></td></tr>"
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
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS &middot; Correo automatico.</p></td></tr>"
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
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;letter-spacing:1px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informacion Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Cita confirmada, " + firstName + "!</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 24px;\">Tu cita ha sido agendada exitosamente. A continuacion los detalles:</p>"
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
            + "<p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS &middot; Correo automatico.</p>"
            + "</td></tr>"
            + "</table></td></tr></table></body></html>";
    }
}