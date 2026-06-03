package com.example.AfriMarket_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${afrimarket.mail.from}")
    private String from;

    @Value("${afrimarket.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String toEmail, String otp, String fullName) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(toEmail);
            helper.setSubject("AfriMarket — Code de réinitialisation de mot de passe");
            helper.setText(buildOtpEmail(fullName, otp), true); // true = HTML
            mailSender.send(mime);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email. Vérifiez votre adresse email.");
        }
    }

    private String buildOtpEmail(String name, String otp) {
        return """
            <div style="font-family:sans-serif; max-width:480px; margin:0 auto; padding:32px 24px; background:#F7F2EA; border-radius:16px;">
              <div style="background:#E8641A; height:4px; border-radius:2px; margin-bottom:28px;"></div>
              <h1 style="font-family:Georgia,serif; color:#1C1208; font-size:24px; margin:0 0 8px;">
                AfriMarket
              </h1>
              <p style="color:#7A6248; font-size:14px; margin:0 0 24px;">Réinitialisation de mot de passe</p>

              <p style="color:#1C1208; font-size:15px;">Bonjour <strong>%s</strong>,</p>
              <p style="color:#1C1208; font-size:15px; line-height:1.6;">
                Voici votre code de vérification pour réinitialiser votre mot de passe AfriMarket.
                Ce code est valable <strong>10 minutes</strong>.
              </p>

              <div style="background:#fff; border-radius:12px; padding:24px; text-align:center; margin:24px 0;
                          border:1px solid rgba(60,40,20,0.12); box-shadow:0 4px 16px rgba(100,60,20,0.08);">
                <div style="letter-spacing:12px; font-size:40px; font-weight:700; color:#E8641A; font-family:monospace;">
                  %s
                </div>
              </div>

              <p style="color:#9CA3AF; font-size:12px; line-height:1.6;">
                Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                Votre mot de passe ne sera pas modifié.
              </p>
              <hr style="border:none; border-top:1px solid rgba(60,40,20,0.12); margin:24px 0;">
              <p style="color:#9CA3AF; font-size:11px; text-align:center;">
                AfriMarket · Yaoundé, Cameroun 🇨🇲
              </p>
            </div>
            """.formatted(
                name != null ? name : "Utilisateur",
                otp
        );
    }
}
