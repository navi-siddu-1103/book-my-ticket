package com.jsp.book.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Component
public class CloudinaryHelper {

	private static final String MOVIE_FOLDER = "BMT-Movies";
	private static final String THEATER_FOLDER = "BMT-Theater";
	private static final String QR_FOLDER = "BMT-Theater-QR";

	private static final String FALLBACK_IMAGE = "https://placehold.co/600x400/EEE/31343C";

	private final Cloudinary cloudinary;

	public CloudinaryHelper(@Value("${cloudinary.url:}") String cloudinaryUrl) {
		this.cloudinary = StringUtils.hasText(cloudinaryUrl) ? new Cloudinary(cloudinaryUrl) : null;
	}

	public String generateImageLink(MultipartFile file) {
		String uploaded = upload(file, MOVIE_FOLDER);
		if (isFallbackImage(uploaded)) {
			return saveLocalMovieImage(file);
		}
		return uploaded;
	}

	public String getTheaterImageLink(MultipartFile file) {
		String uploaded = upload(file, THEATER_FOLDER);
		if (isFallbackImage(uploaded)) {
			return saveLocalTheaterImage(file);
		}
		return uploaded;
	}

	public String saveTicketQr(byte[] qr) {
		return upload(qr, QR_FOLDER);
	}
	
	public boolean isFallbackImage(String link) {
		return FALLBACK_IMAGE.equals(link);
	}

	/* ---------- Private helpers ---------- */

	private String upload(MultipartFile file, String folder) {
		try {
			return upload(file.getBytes(), folder);
		} catch (IOException e) {
			return FALLBACK_IMAGE;
		}
	}

	@SuppressWarnings("unchecked")
	private String upload(byte[] data, String folder) {
		try {
			if (cloudinary == null) {
				return FALLBACK_IMAGE;
			}
			Map<String, Object> params = ObjectUtils.asMap("folder", folder, "use_filename", true, "unique_filename", true,
					"resource_type", "auto");
			Map<String, Object> uploadResult = cloudinary.uploader().upload(data, params);
			Object secureUrl = uploadResult.get("secure_url");
			if (secureUrl instanceof String secureUrlString && StringUtils.hasText(secureUrlString)) {
				return secureUrlString;
			}
			Object url = uploadResult.get("url");
			if (url instanceof String urlString && StringUtils.hasText(urlString)) {
				return urlString;
			}
			return FALLBACK_IMAGE;
		} catch (Exception e) {
			return FALLBACK_IMAGE;
		}
	}

	private String saveLocalMovieImage(MultipartFile file) {
		return saveLocalFile(file, "movies");
	}

	private String saveLocalTheaterImage(MultipartFile file) {
		return saveLocalFile(file, "theaters");
	}

	private String saveLocalFile(MultipartFile file, String folderName) {
		if (file == null || file.isEmpty()) {
			return FALLBACK_IMAGE;
		}

		try {
			String originalName = StringUtils.cleanPath(file.getOriginalFilename());
			String extension = "";
			int lastDot = originalName.lastIndexOf('.');
			if (lastDot > 0 && lastDot < originalName.length() - 1) {
				extension = originalName.substring(lastDot);
			}

			byte[] fileBytes = file.getBytes();
			String fileName = UUID.randomUUID() + extension;
			Path uploadRoot = Path.of(System.getProperty("user.dir"), "uploads");
			Path folderDir = uploadRoot.resolve(folderName);
			Files.createDirectories(folderDir);
			Path target = folderDir.resolve(fileName);
			Files.write(target, fileBytes);

			return "/uploads/" + folderName + "/" + fileName;
		} catch (Exception e) {
			return FALLBACK_IMAGE;
		}
	}
}
