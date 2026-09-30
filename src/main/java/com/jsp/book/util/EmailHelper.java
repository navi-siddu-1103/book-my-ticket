package com.jsp.book.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailHelper {

	private static final String FROM_NAME = "Book-My-Ticket";
	private static final String SUBJECT = "OTP for Creating Account with BookMyTicket";
	private static final String TEMPLATE = "email-template.html";

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${brevo.api-key:}")
	private String brevoApiKey;

	@Value("${resend.api-key:}")
	private String resendApiKey;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	public boolean sendOtp(int otp, String name, String email) {
		String body = renderEmailBody(otp, name);

		// 1. Try Brevo HTTPS API (Port 443 - Never blocked on Render Free Tier)
		if (StringUtils.hasText(brevoApiKey)) {
			if (sendViaBrevoApi(email, name, body)) {
				return true;
			}
		}

		// 2. Try Resend HTTPS API (Port 443 - Never blocked on Render Free Tier)
		if (StringUtils.hasText(resendApiKey)) {
			if (sendViaResendApi(email, body)) {
				return true;
			}
		}

		// 3. Fallback to standard SMTP (Works on localhost or environments allowing port 587)
		return sendViaSmtp(otp, name, email, body);
	}

	private String renderEmailBody(int otp, String name) {
		try {
			Context context = new Context();
			context.setVariable("name", name);
			context.setVariable("otp", otp);
			return templateEngine.process(TEMPLATE, context);
		} catch (Exception e) {
			log.error("Error rendering email template: {}", e.getMessage());
			return "<h2>Hello " + name + "</h2><p>Your OTP for BookMyTicket is: <strong>" + otp + "</strong></p>";
		}
	}

	private boolean sendViaBrevoApi(String toEmail, String toName, String htmlBody) {
		try {
			String safeName = toName != null ? toName.replace("\"", "\\\"") : "User";
			String safeHtml = htmlBody.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "");

			String apiKey = brevoApiKey.trim().replace("\"", "").replace("'", "");
			if (!apiKey.startsWith("xkeysib-")) {
				apiKey = "xkeysib-" + apiKey;
			}

			String payload = "{"
					+ "\"sender\":{\"name\":\"" + FROM_NAME + "\",\"email\":\"" + fromEmail + "\"},"
					+ "\"to\":[{\"email\":\"" + toEmail + "\",\"name\":\"" + safeName + "\"}],"
					+ "\"subject\":\"" + SUBJECT + "\","
					+ "\"htmlContent\":\"" + safeHtml + "\""
					+ "}";

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://api.brevo.com/v3/smtp/email"))
					.header("api-key", apiKey)
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(payload))
					.timeout(Duration.ofSeconds(10))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				log.info("Email sent successfully via Brevo HTTPS API to: {}", toEmail);
				return true;
			} else {
				log.error("Brevo API error (HTTP {}): {}", response.statusCode(), response.body());
				return false;
			}
		} catch (Exception e) {
			log.error("Failed sending via Brevo API: {}", e.getMessage());
			return false;
		}
	}

	private boolean sendViaResendApi(String toEmail, String htmlBody) {
		try {
			String safeHtml = htmlBody.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "");
			String payload = "{"
					+ "\"from\":\"BookMyTicket <onboarding@resend.dev>\","
					+ "\"to\":[\"" + toEmail + "\"],"
					+ "\"subject\":\"" + SUBJECT + "\","
					+ "\"html\":\"" + safeHtml + "\""
					+ "}";

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://api.resend.com/emails"))
					.header("Authorization", "Bearer " + resendApiKey.trim())
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(payload))
					.timeout(Duration.ofSeconds(10))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				log.info("Email sent successfully via Resend HTTPS API to: {}", toEmail);
				return true;
			} else {
				log.error("Resend API error (HTTP {}): {}", response.statusCode(), response.body());
				return false;
			}
		} catch (Exception e) {
			log.error("Failed sending via Resend API: {}", e.getMessage());
			return false;
		}
	}

	private boolean sendViaSmtp(int otp, String name, String email, String body) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(fromEmail, FROM_NAME);
			helper.setTo(email);
			helper.setSubject(SUBJECT);
			helper.setText(body, true);

			mailSender.send(message);
			log.info("Email sent successfully via SMTP to: {}", email);
			return true;
		} catch (Exception ex) {
			log.warn("SMTP send failed for {} (Render Free Tier blocks ports 25/465/587): {}", email, ex.getMessage());
			return false;
		}
	}
}
