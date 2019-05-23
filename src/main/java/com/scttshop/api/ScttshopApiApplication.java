package com.scttshop.api;

import com.scttshop.api.Cache.CacheFactoryManager;
import com.scttshop.api.Controller.CustomerController;
import com.scttshop.api.Entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableCaching
public class ScttshopApiApplication implements CommandLineRunner {

	@Autowired
	private CustomerController customerRepo;



	public static void main(String[] args) {

		SpringApplication.run(ScttshopApiApplication.class, args);

		System.out.println("########## Application started ! ##########");
	}

	@Override
	public void run(String... args) throws Exception {

		Runnable customer = new Runnable() {
			@Override public void run() {
				CacheFactoryManager.CUSTOMER_CACHE = new ConcurrentHashMap<>(customerRepo.getListCustomer()
																			.parallelStream().collect(Collectors.toMap(Customer::getEmail, c->c)));

			}
		};

		new Thread(customer).start();

		}
}
