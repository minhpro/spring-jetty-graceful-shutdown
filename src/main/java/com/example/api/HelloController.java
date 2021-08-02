package com.example.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public String index() {
		return "Hello world!";
	}

	@GetMapping("/heavy")
	public String heavy() {
		try {
			Thread.sleep(20000);
			return "Heavy!";
		} catch (InterruptedException e) {
			return "Heavy interrupted!";
		}
	}
}
