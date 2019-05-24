package com.scttshop.api.Controller;

import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.UserAccount;
import com.scttshop.api.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

import static com.scttshop.api.Cache.CacheFactoryManager.USER_ACCOUNT_CACHE;

@RestController
public class UserAccountController {

    @Autowired
    private UserAccountRepository repo;

    @Autowired
    private EntityManager em;

    @GetMapping("/useraccounts")
    //@Cacheable(value="useraccounts",key="'all'")
    public List<UserAccount> getListUserAccount() {

        try {
            if (USER_ACCOUNT_CACHE != null)
            {
                return new ArrayList<>(USER_ACCOUNT_CACHE.values());
            }

            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController getListProduct ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/useraccounts/{username}")
    //@Cacheable(value="useraccounts",key="#username")
    ResponseEntity findById(@PathVariable("username") String username) {
        try {

            if (USER_ACCOUNT_CACHE!= null && USER_ACCOUNT_CACHE.contains(username))
                return new ResponseEntity(USER_ACCOUNT_CACHE.get(username),HttpStatus.OK);

            Optional<UserAccount> userAccount = repo.findById(username);

            if (userAccount.isPresent()) {
                USER_ACCOUNT_CACHE.putIfAbsent(username,userAccount.get());
                return new ResponseEntity(userAccount.get(), HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/useraccounts")
//    @Caching(
//            put= { @CachePut(value= "useraccounts", key= "#userAccount.username") },
//            evict= { @CacheEvict(value= "useraccounts", key="'all'")}
//    )
    public ResponseEntity insertUserAccount(@Valid @RequestBody UserAccount userAccount){

        try{
            userAccount.setUpdDate(new Timestamp(System.currentTimeMillis()));
            UserAccount res = repo.save(userAccount);

            if (res == null)
                throw new Exception();

            // INSERT CACHE
            USER_ACCOUNT_CACHE.put(res.getUsername(),res);

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController insertCustomer ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/useraccounts/{username}")
//    @Caching(
//            put= { @CachePut(value= "useraccounts", key= "#username") },
//            evict= { @CacheEvict(value= "useraccounts", key="'all'")}
//    )
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

            USER_ACCOUNT_CACHE.replace(updatedUser.getUsername(),updatedUser);

            return new ResponseEntity(updatedUser,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController updateUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/useraccounts/{username}")
//    @Caching(
//            evict= {
//                    @CacheEvict(value="useraccounts",key="#username"),
//                    @CacheEvict(value= "useraccounts", key="'all'")
//            }
//    )
    public ResponseEntity deleteUserAccount(@PathVariable(value = "username") String username){

        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            USER_ACCOUNT_CACHE.remove(username);

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController deleteUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
