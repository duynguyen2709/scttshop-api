package com.scttshop.api.Repository;

import com.scttshop.api.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,String> {
    List<Order> findByEmail(String email);
}
