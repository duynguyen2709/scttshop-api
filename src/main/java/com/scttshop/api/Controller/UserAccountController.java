package com.scttshop.api.Controller;

import com.scttshop.api.Entity.*;
import com.scttshop.api.Repository.ProductRepository;
import com.scttshop.api.Repository.PromotionRepository;
import com.scttshop.api.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class UserAccountController {

    @Autowired
    private UserAccountRepository repo;

    @Autowired
    private EntityManager em;

    @GetMapping("/useraccounts")
    @Cacheable(value="useraccounts",key="'all'")
    List<UserAccount> getListUserAccount() {

        try {
            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController findAll ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/useraccounts/{username}")
    @Cacheable(value="useraccounts",key="#username")
    ResponseEntity findById(@PathVariable("username") String username) {
        try {
            Optional<UserAccount> userAccount = repo.findById(username);

            if (userAccount.isPresent()) {
                return new ResponseEntity(userAccount, HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.NOT_FOUND);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/useraccounts")
    @Caching(
            put= { @CachePut(value= "useraccounts", key= "#userAccount.username") },
            evict= { @CacheEvict(value= "useraccounts", key="'all'")}
    )
    public ResponseEntity insertUserAccount(@Valid @RequestBody UserAccount userAccount){

        try{
            userAccount.setUpdDate(new Timestamp(System.currentTimeMillis()));
            UserAccount res = repo.save(userAccount);

            if (res == null)
                throw new Exception();

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController insertUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/useraccounts/{username}")
    @Caching(
            put= { @CachePut(value= "useraccounts", key= "#username") },
            evict= { @CacheEvict(value= "useraccounts", key="'all'")}
    )
    public ResponseEntity updateUserAccount(@PathVariable(value = "username") String username,
                                          @Valid @RequestBody UserAccount userAccount){
        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(userAccount);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            UserAccount updatedUser = repo.save(old.get());

            if (updatedUser == null)
                throw new Exception();

            return new ResponseEntity(updatedUser,HttpStatus.OK);

        }
//        catch (ChangeSetPersister.NotFoundException e){
//            System.out.println(e.getMessage());
//            return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.NOT_FOUND);
//        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController updateUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/useraccounts/{username}")
    @Caching(
            evict= {
                    @CacheEvict(value="useraccounts",key="#username"),
                    @CacheEvict(value= "useraccounts", key="'all'")
            }
    )
    public ResponseEntity deleteUserAccount(@PathVariable(value = "username") String username){

        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController deleteUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
