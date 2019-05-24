package com.scttshop.api.Controller;

import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.Comment;
import com.scttshop.api.Repository.CommentRepository;
import com.scttshop.api.Repository.CommentRepository;
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

import static com.scttshop.api.Cache.CacheFactoryManager.COMMENT_CACHE;

@RestController
public class CommentController {

    @Autowired
    private CommentRepository repo;


    @GetMapping("/comments")
    //@Cacheable(value="comments",key="'all'")
    public List<Comment> getListComment() {

        try {
            if (COMMENT_CACHE != null)
            {
                return new ArrayList<>(COMMENT_CACHE.values());
            }

            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("CommentController getListComment ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/comments/{commentID}")
    //@Cacheable(value="comments",key="#username")
    ResponseEntity findById(@PathVariable("commentID") Integer commentID) {
        try {

            if (COMMENT_CACHE!= null && COMMENT_CACHE.contains(commentID))
                return new ResponseEntity(COMMENT_CACHE.get(commentID),HttpStatus.OK);

            Optional<Comment> comment = repo.findById(commentID);

            if (comment.isPresent()) {
                COMMENT_CACHE.putIfAbsent(commentID,comment.get());
                return new ResponseEntity(comment.get(), HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CommentController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/comments")
//    @Caching(
//            put= { @CachePut(value= "comments", key= "#userAccount.username") },
//            evict= { @CacheEvict(value= "comments", key="'all'")}
//    )
    public ResponseEntity insertComment(@Valid @RequestBody Comment comment){

        try{
            if (comment.getCommentTime() == null || comment.getCommentTime().isEmpty())
                comment.setCommentTime(new Timestamp(System.currentTimeMillis()));

            comment.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Comment res = repo.save(comment);

            if (res == null)
                throw new Exception();

            // INSERT CACHE
            COMMENT_CACHE.put(res.getCommentID(),res);

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("CommentController insertCustomer ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/comments/{commentID}")
//    @Caching(
//            put= { @CachePut(value= "comments", key= "#username") },
//            evict= { @CacheEvict(value= "comments", key="'all'")}
//    )
    public ResponseEntity updateComment(@PathVariable(value = "commentID") Integer commentID,
                                          @Valid @RequestBody Comment comment){
        try{
            Optional<Comment> old = repo.findById(commentID);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(comment);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            Comment updatedComment = repo.save(old.get());

            if (updatedComment == null)
                throw new Exception();

            COMMENT_CACHE.replace(updatedComment.getCommentID(),updatedComment);

            return new ResponseEntity(updatedComment,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CommentController updateComment ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/comments/{commentID}")
//    @Caching(
//            evict= {
//                    @CacheEvict(value="comments",key="#username"),
//                    @CacheEvict(value= "comments", key="'all'")
//            }
//    )
    public ResponseEntity deleteComment(@PathVariable(value = "commentID") Integer commentID){

        try{
            Optional<Comment> old = repo.findById(commentID);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            COMMENT_CACHE.remove(commentID);

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CommentController deleteComment ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
