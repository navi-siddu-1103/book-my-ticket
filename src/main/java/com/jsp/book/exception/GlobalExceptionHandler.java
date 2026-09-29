package com.jsp.book.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(NoResourceFoundException.class)
	public String handleNoResourceFound(NoResourceFoundException ex) {
		return "404";
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String handleMaxSizeException(MaxUploadSizeExceededException ex, RedirectAttributes attributes,
			HttpServletRequest request) {
		log.warn("Uploaded file exceeds maximum limit: {}", ex.getMessage());
		attributes.addFlashAttribute("fail", "File is too large! Maximum allowed upload size is 25MB.");
		String referer = request.getHeader("Referer");
		return referer != null ? "redirect:" + referer : "redirect:/manage-movies";
	}

	@ExceptionHandler(Exception.class)
	public String handleGeneralException(Exception ex, RedirectAttributes attributes, HttpServletRequest request) {
		log.error("Unhandled exception caught by GlobalExceptionHandler: {}", ex.getMessage(), ex);
		attributes.addFlashAttribute("fail", "Error: " + ex.getMessage());
		String referer = request.getHeader("Referer");
		return referer != null ? "redirect:" + referer : "redirect:/manage-movies";
	}
}
