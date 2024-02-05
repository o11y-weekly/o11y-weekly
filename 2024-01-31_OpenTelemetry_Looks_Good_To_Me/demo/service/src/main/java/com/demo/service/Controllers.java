package com.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public record Controllers(@Autowired JdbcTemplate jdbcTemplate) {
	private static record User(String name, String surname) {
	}

	private static class UserMapper implements RowMapper<User> {
		@Override
		public User mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			return new User(rs.getString("firstname"), rs.getString("surname"));
		}
	}

	@ResponseStatus(value = HttpStatus.NOT_FOUND)
    private static class ResourceNotFoundException extends RuntimeException {
    }

	private static final Logger logger = LoggerFactory.getLogger(Controllers.class);
	private static final Random random = new Random(0);

	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	private static final UserMapper USER_MAPPER = new UserMapper(); 

	private static final Double FAILURE_RATIO = Optional.ofNullable(System.getenv("FAILURE_RATIO")).map(Double::parseDouble).orElseThrow();
	private static final Integer MIN_LATENCY = Optional.ofNullable(System.getenv("MIN_LATENCY")).map(Integer::parseInt).orElseThrow();
	private static final Integer MAX_LATENCY = Optional.ofNullable(System.getenv("MAX_LATENCY")).map(Integer::parseInt).orElseThrow();

	@WithSpan
	private Optional<User> getUserFromDatabase(final int id) {
		logger.info("getting user {} from database", id);
		return jdbcTemplate.query("select firstname, surname from persons where id=?", USER_MAPPER, id).stream().findFirst();
	}

	@WithSpan
	private User getFailedUserFromDatabase(final int id) {
		logger.info("(will fail) getting user {} from database", id);
		return jdbcTemplate.queryForObject("select firstname, surname from bad_table where id=?", USER_MAPPER, id);
	}

	@WithSpan
	private void createUserIntoDatabase(final User user) {
		jdbcTemplate.update("insert into persons(firstname, surname) values(?, ?)", user.name, user.surname);
	}

	private static int getRandom(final int min, final int max) {
		return random.nextInt(max - min + 1) + min;
	}

	@WithSpan
	private static void callSlowDependency(final int latency) throws InterruptedException {
		Thread.sleep(latency);
	}

	@GetMapping(path = "/user")
	User getUser(final @RequestParam("id") Integer id) throws InterruptedException {
		final var counter = COUNTER.getAndIncrement();
		final var timing = getRandom(MIN_LATENCY, MAX_LATENCY);

		callSlowDependency(timing);
		logger.info("/user has been called!");
		if (counter % (1 / FAILURE_RATIO) == 0) {
			return getFailedUserFromDatabase(id);
		}

		return getUserFromDatabase(id).orElseThrow(ResourceNotFoundException::new);
	}

	@PostMapping(path = "/user")
	@ResponseStatus(value = HttpStatus.CREATED)
	void createUser(final @RequestBody User user) {
		createUserIntoDatabase(user);
	}
}