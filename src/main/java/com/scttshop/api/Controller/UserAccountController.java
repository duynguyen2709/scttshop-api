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
import java.util.stream.Collectors;

import static com.scttshop.api.Cache.CacheFactoryManager.USER_ACCOUNT_CACHE;

@RestController
public class UserAccountController {

    @Autowired
    private UserAccountRepository repo;

    @Autowired
    private EntityManager em;

    @GetMapping("/useraccounts")
    public List<UserAccount> getListUserAccount(@RequestParam(required = false,defaultValue = "false") Boolean notVerified) {

        try {
            if (USER_ACCOUNT_CACHE != null)
            {
                List<UserAccount> userAccounts = new ArrayList<>(USER_ACCOUNT_CACHE.values());

                if (notVerified != null && notVerified){
                    userAccounts = userAccounts.stream().filter(c -> c.getIsVerified() == 0).collect(Collectors.toList());
                }

                return  userAccounts;

            }

            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController getListProduct ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/useraccounts/{username}")
    ResponseEntity findById(@PathVariable("username") String username) {
        try {

            if (USER_ACCOUNT_CACHE!= null) {

                UserAccount userAccount = USER_ACCOUNT_CACHE.get(username);

                return (userAccount == null ?  new  ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK)
                        : new ResponseEntity<>(userAccount,HttpStatus.OK));

            }

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
    public ResponseEntity insertUserAccount(@Valid @RequestBody UserAccount userAccount){

        try{
            if (USER_ACCOUNT_CACHE.contains(userAccount.getUsername())
                    || USER_ACCOUNT_CACHE.get(userAccount.getUsername()) != null
                    || repo.findById(userAccount.getUsername()).isPresent()){
                return new ResponseEntity("Username Already Existed.",HttpStatus.OK);
            }

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
    public ResponseEntity updateUserAccount(@PathVariable(value = "username") String username,
                                          @Valid @RequestBody UserAccount userAccount){
        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

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

    @PutMapping("/useraccounts/{username}/lock/{status}")
    public ResponseEntity lockUserAccount(@PathVariable(value = "username") String username,
                                          @PathVariable(value = "status") int status){
        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

            old.get().setStatus(status);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            UserAccount updatedUser = repo.save(old.get());

            if (updatedUser == null)
                throw new Exception();

            USER_ACCOUNT_CACHE.replace(updatedUser.getUsername(),updatedUser);

            return new ResponseEntity(updatedUser,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController lockUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/useraccounts/{username}/verify")
    public ResponseEntity verifyAccount(@PathVariable(value = "username") String username){
        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

            old.get().setIsVerified(1);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            UserAccount updatedUser = repo.save(old.get());

            if (updatedUser == null)
                throw new Exception();

            USER_ACCOUNT_CACHE.replace(updatedUser.getUsername(),updatedUser);

            return new ResponseEntity(updatedUser,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController lockUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/useraccounts/{username}")
    public ResponseEntity deleteUserAccount(@PathVariable(value = "username") String username){

        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

            repo.delete(old.get());

            USER_ACCOUNT_CACHE.remove(username);

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController deleteUserAccount ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/useraccounts/{username}/logon")
    public ResponseEntity loginSucceed(@PathVariable(value="username") String username){
        try{
            Optional<UserAccount> old = repo.findById(username);

            if (!old.isPresent())
            {
                return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);
            }

            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));
            old.get().setLastLoginTime(new Timestamp(System.currentTimeMillis()));
            UserAccount updatedUser = repo.save(old.get());

            USER_ACCOUNT_CACHE.replace(username,updatedUser);

            return new ResponseEntity(USER_ACCOUNT_CACHE.get(username),HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController loginSucceed ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("useraccounts/checkvalid")
    public ResponseEntity checkUserNotExist(@RequestParam(required = false) String username,@RequestParam(required = false) String email){
        try {
            if ((username == null && email == null) || (username != null && username.isEmpty() && email != null && email.isEmpty()))
                return new ResponseEntity(0, HttpStatus.OK);

            if (username != null && !username.isEmpty()) {
                UserAccount userAccount = USER_ACCOUNT_CACHE.get(username);

                if (userAccount != null)
                    return new ResponseEntity(-2, HttpStatus.OK);

            }

            if (email != null && !email.isEmpty()) {
                for (UserAccount c : USER_ACCOUNT_CACHE.values())
                    if (c.getEmail().equalsIgnoreCase(email))
                        //email already existed
                        return new ResponseEntity(-1, HttpStatus.OK);
            }

            return new ResponseEntity(1, HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("UserAccountController checkUserNotExist ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

    }
    
    
}
