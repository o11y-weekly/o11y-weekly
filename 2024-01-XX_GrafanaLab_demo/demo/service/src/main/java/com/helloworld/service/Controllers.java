package com.helloworld.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Value;

@RestController
public class Controllers {
	private static class ControllerException extends RuntimeException {
		public ControllerException(final String message) {
			super(message);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(Controllers.class);
	private static final Logger customLogger = LoggerFactory.getLogger("custom_logger");
	private static final Random random = new Random(0);

	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	@Value
	static class User {
		public User(final String name, final String surname) {
			this.name = name;
			this.surname = surname;
		}

		String name;
		String surname;
	}

	@WithSpan
	private static User getSpringGuruUser() {
		return new User("Spring", "Guru");
	}

	private static int getRandom(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	@WithSpan
	private static void callSlowDependency(final int latency) throws InterruptedException {
		Thread.sleep(latency);
	}

	@WithSpan
	private static void callFailure(final int counter) {
		if (counter % 10 == 0) {
			throw new ControllerException("boom!");
		}
	}

	@GetMapping(path = "/user")
	User getUser(@RequestParam("id") Integer id) throws InterruptedException {
		final var counter = COUNTER.getAndIncrement();
		final var timing = getRandom(0, 1000);

		callSlowDependency(timing);
		callFailure(counter);
		customLogger.info("bad logs");
		customLogger.info("H={}\tT={}", id, timing);
		logger.info("/user has been called!");
		return getSpringGuruUser();
	}
}