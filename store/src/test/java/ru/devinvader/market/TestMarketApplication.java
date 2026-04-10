package ru.devinvader.market;

import org.springframework.boot.SpringApplication;

public class TestMarketApplication {

	public static void main(String[] args) {
		SpringApplication.from(MarketApplication::main).run(args);
	}

}
