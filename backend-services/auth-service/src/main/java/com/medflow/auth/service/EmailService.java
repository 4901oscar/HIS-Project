package com.medflow.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendActivationEmail(String toEmail, String firstName, String token) {
        String activationLink = frontendUrl + "/activate?token=" + token;
        send(toEmail, "Activa tu cuenta en MedFlow HIS",
                buildActivationHtml(firstName, activationLink));
    }

    @Async
    public void sendTempPasswordEmail(String toEmail, String firstName,
                                      String username, String tempPassword) {
        send(toEmail, "Bienvenido a MedFlow HIS — Credenciales de acceso",
                buildTempPasswordHtml(firstName, username, tempPassword, frontendUrl + "/login"));
    }

    @Async
    public void sendEmployeeCreatedEmail(String toEmail, String firstName,
                                         String username, String tempPassword) {
        send(toEmail, "Cuenta de empleado creada — MedFlow HIS",
                buildEmployeeHtml(firstName, username, tempPassword, frontendUrl + "/login"));
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress, "MedFlow HIS");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("[Email] Enviado a {} — {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[Email] Error al enviar a {}: {}", to, e.getMessage());
        }
    }

    private String buildActivationHtml(String firstName, String link) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;letter-spacing:1px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informaci\u00f3n Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Hola, " + firstName + " \uD83D\uDC4B</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 24px;\">Gracias por registrarte en MedFlow HIS. Para activar tu cuenta haz clic en el bot\u00f3n:</p>"
            + "<div style=\"text-align:center;margin:0 0 28px;\">"
            + "<a href=\"" + link + "\" style=\"display:inline-block;background:#00d4e8;color:#0f4c75;font-weight:bold;font-size:15px;padding:14px 32px;border-radius:6px;text-decoration:none;\">Activar mi cuenta</a></div>"
            + "<p style=\"color:#888;font-size:12px;margin:0 0 8px;\">El enlace es v\u00e1lido por <strong>24 horas</strong>. Si no creaste esta cuenta, ignora este correo.</p>"
            + "<p style=\"color:#bbb;font-size:11px;margin:0;\">Si el bot\u00f3n no funciona copia este enlace:<br><span style=\"color:#00d4e8;\">" + link + "</span></p>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS &middot; Correo autom\u00e1tico.</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    private String buildTempPasswordHtml(String firstName, String username,
                                          String tempPassword, String loginUrl) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informaci\u00f3n Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Bienvenido/a, " + firstName + "</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 20px;\">Tu cuenta de paciente ha sido creada. Tus credenciales de acceso temporales:</p>"
            + "<table style=\"background:#f0faff;border:1px solid #b8e4f0;border-radius:6px;width:100%;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:14px 20px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Usuario (DPI):</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:16px;color:#0f4c75;font-weight:bold;\">" + username + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Contrase\u00f1a temporal:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:20px;color:#0f4c75;font-weight:bold;letter-spacing:2px;\">" + tempPassword + "</p></td></tr></table>"
            + "<p style=\"color:#e74c3c;font-size:13px;margin:0 0 24px;\">\u26a0\ufe0f Cambia tu contrase\u00f1a en el primer inicio de sesi\u00f3n.</p>"
            + "<div style=\"text-align:center;\"><a href=\"" + loginUrl + "\" style=\"display:inline-block;background:#00d4e8;color:#0f4c75;font-weight:bold;font-size:15px;padding:14px 32px;border-radius:6px;text-decoration:none;\">Iniciar sesi\u00f3n</a></div>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS &middot; Correo autom\u00e1tico.</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    private String buildEmployeeHtml(String firstName, String username,
                                      String tempPassword, String loginUrl) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;padding:40px 0;\"><tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);\">"
            + "<tr><td style=\"background:#0f4c75;padding:28px 40px;\">"
            + "<h1 style=\"margin:0;color:#00d4e8;font-size:22px;\">MedFlow HIS</h1>"
            + "<p style=\"margin:4px 0 0;color:#b0d4e8;font-size:12px;\">Sistema de Informaci\u00f3n Hospitalaria</p></td></tr>"
            + "<tr><td style=\"padding:36px 40px;\">"
            + "<h2 style=\"margin:0 0 16px;color:#1a1a2e;font-size:20px;\">Cuenta creada, " + firstName + "</h2>"
            + "<p style=\"color:#444;line-height:1.6;margin:0 0 20px;\">El administrador ha creado tu cuenta de empleado en MedFlow HIS:</p>"
            + "<table style=\"background:#f0faff;border:1px solid #b8e4f0;border-radius:6px;width:100%;margin:0 0 24px;\">"
            + "<tr><td style=\"padding:14px 20px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Usuario:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:16px;color:#0f4c75;font-weight:bold;\">" + username + "</p></td></tr>"
            + "<tr><td style=\"padding:0 20px 14px;\"><p style=\"margin:0 0 6px;color:#555;font-size:13px;\"><strong>Contrase\u00f1a temporal:</strong></p>"
            + "<p style=\"margin:0;font-family:monospace;font-size:20px;color:#0f4c75;font-weight:bold;letter-spacing:2px;\">" + tempPassword + "</p></td></tr></table>"
            + "<p style=\"color:#e74c3c;font-size:13px;margin:0 0 24px;\">\u26a0\ufe0f Cambia tu contrase\u00f1a en tu primer inicio de sesi\u00f3n.</p>"
            + "<div style=\"text-align:center;\"><a href=\"" + loginUrl + "\" style=\"display:inline-block;background:#00d4e8;color:#0f4c75;font-weight:bold;font-size:15px;padding:14px 32px;border-radius:6px;text-decoration:none;\">Acceder al sistema</a></div>"
            + "</td></tr>"
            + "<tr><td style=\"background:#f4f6f8;padding:16px 40px;text-align:center;\"><p style=\"margin:0;color:#aaa;font-size:11px;\">&copy; 2025 MedFlow HIS &middot; Correo autom\u00e1tico.</p></td></tr>"
            + "</table></td></tr></table></body></html>";
    }
}
