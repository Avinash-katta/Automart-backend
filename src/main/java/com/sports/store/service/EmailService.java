package com.sports.store.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String toEmail, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Password Reset Request - SalesSavvy Automobile Store");

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        String htmlContent = "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px; border: 1px solid #e5e7eb; border-radius: 8px; background-color: #ffffff;\">"
                + "  <h2 style=\"color: #111827; font-size: 24px; font-weight: 600; margin-bottom: 24px; text-align: center; letter-spacing: -0.5px;\">SALESSAVVY AUTOMOBILE STORE</h2>"
                + "  <p style=\"color: #4b5563; font-size: 16px; line-height: 24px; margin-bottom: 24px;\">Hello,</p>"
                + "  <p style=\"color: #4b5563; font-size: 16px; line-height: 24px; margin-bottom: 32px;\">We received a request to reset your password. Click the button below to choose a new password. This link is valid for 15 minutes.</p>"
                + "  <div style=\"text-align: center; margin-bottom: 32px;\">"
                + "    <a href=\"" + resetLink + "\" style=\"display: inline-block; background-color: #000000; color: #ffffff; text-decoration: none; padding: 14px 30px; font-size: 16px; font-weight: 500; border-radius: 4px; letter-spacing: 0.5px;\">Change Password</a>"
                + "  </div>"
                + "  <p style=\"color: #4b5563; font-size: 14px; line-height: 20px; margin-bottom: 24px;\">If the button above does not work, copy and paste this URL into your browser:</p>"
                + "  <p style=\"color: #2563eb; font-size: 14px; line-height: 20px; word-break: break-all; margin-bottom: 32px;\">" + resetLink + "</p>"
                + "  <hr style=\"border: 0; border-top: 1px solid #e5e7eb; margin-bottom: 24px;\" />"
                + "  <p style=\"color: #9ca3af; font-size: 12px; line-height: 16px; text-align: center;\">If you did not request a password reset, please ignore this email.</p>"
                + "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
