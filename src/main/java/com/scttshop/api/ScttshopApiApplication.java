package com.scttshop.api;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.scttshop.api.Controller.CategoryController;
import com.scttshop.api.Controller.CustomerController;
import com.scttshop.api.Controller.ProductController;
import com.scttshop.api.Repository.CategoryRepository;
import com.scttshop.api.Repository.CommentRepository;
import com.scttshop.api.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

@SpringBootApplication
@EnableCaching
public class ScttshopApiApplication implements CommandLineRunner {

	@Autowired
	private CustomerController repo;

	public static void main(String[] args) {

		SpringApplication.run(ScttshopApiApplication.class, args);

		System.out.println("########## Application started ! ##########");
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println(repo.getListCustomer());
	}
}
