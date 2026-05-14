package com.jotechhub.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reset your JoTech Hub password");

        message.setText("""
                Hello,

                We received a request to reset your JoTech Hub password.

                Click the link below to set a new password:
                %s

                This link will expire soon.

                If you did not request this, please ignore this email.

                JoTech Hub Team
                """.formatted(resetLink));

        javaMailSender.send(message);
    }
}