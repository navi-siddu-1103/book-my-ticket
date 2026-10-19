package com.jsp.book.service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.BookedTicket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

	private static final String USER_DTO_KEY = "dto-";
	private static final String OTP_KEY = "otp-";

	private static final Duration USER_DTO_TTL = Duration.ofMinutes(15);
	private static final Duration OTP_TTL = Duration.ofMinutes(2);
	private static final Duration TICKET_TTL = Duration.ofMinutes(15);

	private final RedisTemplate<String, Object> redisTemplate;

	/* ---------- In-memory fallback (used when Redis is unavailable) ---------- */
	private final ConcurrentHashMap<String, Object> fallbackStore = new ConcurrentHashMap<>();

	// ─── Save helpers ────────────────────────────────────────────────────────────

	@Override
	public void saveUserDto(String email, UserDto userDto) {
		String key = USER_DTO_KEY + email;
		try {
			redisTemplate.opsForValue().set(key, userDto, USER_DTO_TTL);
		} catch (Exception e) {
			log.warn("Redis unavailable – storing UserDto in memory for {}", email);
			fallbackStore.put(key, userDto);
		}
	}

	@Override
	public void saveOtp(String email, int otp) {
		String key = OTP_KEY + email;
		try {
			redisTemplate.opsForValue().set(key, otp, OTP_TTL);
		} catch (Exception e) {
			log.warn("Redis unavailable – storing OTP in memory for {}", email);
			fallbackStore.put(key, otp);
		}
	}

	@Override
	public void saveTicket(String orderId, BookedTicket ticket) {
		try {
			redisTemplate.opsForValue().set(orderId, ticket, TICKET_TTL);
		} catch (Exception e) {
			log.warn("Redis unavailable – storing ticket in memory for orderId {}", orderId);
			fallbackStore.put(orderId, ticket);
		}
	}

	// ─── Get helpers ─────────────────────────────────────────────────────────────

	@Override
	public UserDto getUserDto(String email) {
		String key = USER_DTO_KEY + email;
		try {
			Object value = redisTemplate.opsForValue().get(key);
			if (value instanceof UserDto dto) return dto;
		} catch (Exception e) {
			log.warn("Redis unavailable – reading UserDto from memory for {}", email);
		}
		Object fallback = fallbackStore.get(key);
		return (fallback instanceof UserDto dto) ? dto : null;
	}

	@Override
	public int getOtp(String email) {
		String key = OTP_KEY + email;
		try {
			Object value = redisTemplate.opsForValue().get(key);
			if (value instanceof Integer otp) return otp;
		} catch (Exception e) {
			log.warn("Redis unavailable – reading OTP from memory for {}", email);
		}
		Object fallback = fallbackStore.get(key);
		return (fallback instanceof Integer otp) ? otp : 0;
	}

	@Override
	public BookedTicket getTicket(String orderId) {
		try {
			Object value = redisTemplate.opsForValue().get(orderId);
			if (value instanceof BookedTicket ticket) return ticket;
		} catch (Exception e) {
			log.warn("Redis unavailable – reading ticket from memory for orderId {}", orderId);
		}
		Object fallback = fallbackStore.get(orderId);
		return (fallback instanceof BookedTicket ticket) ? ticket : null;
	}
}

