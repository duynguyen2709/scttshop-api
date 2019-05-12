package com.scttshop.api;

import com.scttshop.api.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
public class ScttshopApiApplication implements CommandLineRunner {


	public static void main(String[] args) {

		SpringApplication.run(ScttshopApiApplication.class, args);
	}

	@Override public void run(String... args) throws Exception {

	}
}
