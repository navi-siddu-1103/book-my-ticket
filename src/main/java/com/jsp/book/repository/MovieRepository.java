package com.jsp.book.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
	List<Movie> findAllByOrderByReleaseDateDesc();

	boolean existsByNameAndReleaseDate(String name, LocalDate releaseDate);
}
