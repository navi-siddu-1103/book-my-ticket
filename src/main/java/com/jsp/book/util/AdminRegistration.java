package com.jsp.book.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jsp.book.entity.Movie;
import com.jsp.book.entity.User;
import com.jsp.book.repository.MovieRepository;
import com.jsp.book.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminRegistration implements CommandLineRunner {

	@Value("${admin.email}")
	private String adminEmail;

	@Value("${admin.password}")
	private String adminPassword;

	private final UserRepository userRepository;
	private final MovieRepository movieRepository;

	@Override
	public void run(String... args) {

		if (!userRepository.existsByEmail(adminEmail)) {
			User adminUser = new User(null, "ADMIN", adminEmail, 0L, AES.encrypt(adminPassword), "ADMIN", false);
			userRepository.save(adminUser);
			log.info("Admin registration successful");
		} else {
			log.info("Admin already exists");
		}

		repairBrokenMoviePosters();
	}

	private void repairBrokenMoviePosters() {
		try {
			List<Movie> movies = movieRepository.findAll();
			for (Movie movie : movies) {
				String link = movie.getImageLink();
				if (link != null && (link.startsWith("/uploads/") || link.contains("placehold.co"))) {
					String name = movie.getName() != null ? movie.getName().toLowerCase() : "";
					if (name.contains("interstellar")) {
						movie.setImageLink("https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg");
					} else if (name.contains("inception")) {
						movie.setImageLink("https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg");
					} else if (name.contains("with love")) {
						movie.setImageLink("https://images.unsplash.com/photo-1518676599625-5835b4145df4?w=800&q=80");
					} else {
						movie.setImageLink("https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&q=80");
					}
					movieRepository.save(movie);
					log.info("Auto-repaired poster for movie '{}' -> {}", movie.getName(), movie.getImageLink());
				}
			}
		} catch (Exception e) {
			log.warn("Failed to auto-repair movie posters: {}", e.getMessage());
		}
	}
}
