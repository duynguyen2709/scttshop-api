package com.scttshop.api.Controller;

import com.scttshop.api.Entity.Customer;
import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.Order;
import com.scttshop.api.Repository.CustomerRepository;
import com.scttshop.api.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.scttshop.api.Cache.CacheFactoryManager.CUSTOMER_CACHE;
import static com.scttshop.api.Cache.CacheFactoryManager.ORDER_LOG_CACHE;

@RestController
public class OrderController {

    @Autowired
    private OrderRepository repo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private EntityManager em;

    @GetMapping("/orders")
    public List<Order> findAll() {

        try {
            if (ORDER_LOG_CACHE != null)
            {
                return new ArrayList<>(ORDER_LOG_CACHE.values());
            }

            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("OrderController findAll ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/orders/{orderID}")
    public ResponseEntity findById(@PathVariable("orderID") String orderID) {
        try {

            if (ORDER_LOG_CACHE!= null) {

                Order order = ORDER_LOG_CACHE.get(orderID);

                return (order == null ? new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK):
                        new ResponseEntity(order, HttpStatus.OK));

            }

            Optional<Order> order = repo.findById(orderID);

            if (order.isPresent()) {
                ORDER_LOG_CACHE.putIfAbsent(orderID,order.get());
                return new ResponseEntity(order.get(), HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("OrderController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/orders")
    public ResponseEntity insertOrder(@Valid @RequestBody Order order){

        try{
            order.setOrderID(getLastOrderID());
            order.setOrderTime(new Timestamp(System.currentTimeMillis()));
            order.setUpdDate(new Timestamp(System.currentTimeMillis()));
            order.setStatus("PROCESSING");

            Order res = repo.save(order);

            Optional<Customer> customer = customerRepo.findById(res.getEmail());
            if (!customer.isPresent())
                throw new Exception("Customer Not Found");

            customer.get().setTotalBuy(customer.get().getTotalBuy() + res.getTotalPrice());
            Customer save = customerRepo.save(customer.get());

            if (save == null)
                throw new Exception();

            // INSERT CACHE
            ORDER_LOG_CACHE.put(res.getOrderID(),res);
            CUSTOMER_CACHE.replace(save.getEmail(),save);

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("OrderController insertOrder ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    private String getLastOrderID() {

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

        String query = "SELECT CAST(orderID AS UNSIGNED) as id FROM OrderLog " +
                "WHERE orderID LIKE '" + today + "' ORDER BY id desc LIMIT 1";
        try {
            String lastOrderID = String.valueOf(em.createNativeQuery(query).getSingleResult());

            if (lastOrderID == null || lastOrderID.isEmpty()) {
                return (today + "0001");
            }
            return String.valueOf(Long.parseLong(lastOrderID) + 1);
        }
        catch (Exception e){
            return (today + "0001");
        }
    }

    @PutMapping("/orders/{orderID}")
    public ResponseEntity updateOrder(@PathVariable(value = "orderID") String orderID,
                                          @Valid @RequestBody Order order){
        try{
            Optional<Order> old = repo.findById(orderID);

            if (!old.isPresent())
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

            old.get().copyFieldValues(order);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            Order updatedOrder = repo.save(old.get());

            if (updatedOrder == null)
                throw new Exception();

            ORDER_LOG_CACHE.replace(updatedOrder.getOrderID(),updatedOrder);

            return new ResponseEntity(updatedOrder,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("OrderController updateOrder ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/orders/{orderID}")
    public ResponseEntity deleteOrder(@PathVariable(value = "orderID") String orderID){

        try{
            Optional<Order> old = repo.findById(orderID);

            if (!old.isPresent())
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

            repo.delete(old.get());

            ORDER_LOG_CACHE.remove(orderID);

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("OrderController deleteOrder ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/orders/{orderID}/cancel")
    public ResponseEntity cancelOrder(@PathVariable(value="orderID") String orderID){
        
        try{
            Optional<Order> old = repo.findById(orderID);

            if (!old.isPresent())
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

            old.get().setStatus("CANCELLED");
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            Order updatedOrder = repo.save(old.get());

            if (updatedOrder == null)
                throw new Exception();

            ORDER_LOG_CACHE.replace(updatedOrder.getOrderID(),updatedOrder);

            return new ResponseEntity(updatedOrder,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("OrderController updateOrder ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


}
