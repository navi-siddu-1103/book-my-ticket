package com.jsp.book.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailHelper {

	private static final String FROM_NAME = "Book-My-Ticket";
	private static final String SUBJECT = "Otp for Creating Account with BookMyTicket";
	private static final String TEMPLATE = "email-template.html";

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public boolean sendOtp(int otp, String name, String email) {

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(fromEmail, FROM_NAME);
			helper.setTo(email);
			helper.setSubject(SUBJECT);

			Context context = new Context();
			context.setVariable("name", name);
			context.setVariable("otp", otp);

			String body = templateEngine.process(TEMPLATE, context);
			helper.setText(body, true);

			mailSender.send(message);
			return true;

		} catch (Exception ex) {
			System.err.println("Failed to send OTP mail for email: " + email + ": " + ex.getMessage());
			return false;
		}
	}
}
