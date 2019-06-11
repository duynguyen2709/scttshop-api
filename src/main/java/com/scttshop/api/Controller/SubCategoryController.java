package com.scttshop.api.Controller;

import com.scttshop.api.Entity.Category;
import com.scttshop.api.Entity.Comment;
import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.SubCategory;
import com.scttshop.api.Repository.CommentRepository;
import com.scttshop.api.Repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;

import static com.scttshop.api.Cache.CacheFactoryManager.*;

@RestController
public class SubCategoryController {

    @Autowired
    private SubCategoryRepository repo;

    @GetMapping("/subcategories")
    public List<SubCategory> getListSubCategories() {

        try {
            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("SubCategoryController getListSubCategories ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/subcategories/{subCategoryID}")
    ResponseEntity findById(@PathVariable("subCategoryID") Integer subCategoryID) {
        try {

            Optional<SubCategory> subCategory = repo.findById(subCategoryID);

            if (subCategory.isPresent()) {
                 return new ResponseEntity(subCategory.get(), HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("SubCategoryController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/subcategories")
    public ResponseEntity insertSubCategory(@Valid @RequestBody SubCategory subCategory){

        try{

            SubCategory res = repo.save(subCategory);

            if (res == null)
                throw new Exception();

            CATEGORY_CACHE.get(res.getCategoryID()).getSubCategories().add(res);

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("SubCategoryController insertSubCategory ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/subcategories/{subCategoryID}")
    public ResponseEntity updateSubCategory(@PathVariable(value = "subCategoryID") Integer subCategoryID,
                                          @Valid @RequestBody SubCategory subCategory){
        try{
            Optional<SubCategory> old = repo.findById(subCategoryID);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(subCategory);

            SubCategory newEnt = repo.save(old.get());

            if (newEnt == null)
                throw new Exception();

            Collection<Category> values = CATEGORY_CACHE.values();

            for (Category entity : values){
                for (SubCategory sub:entity.getSubCategories()){
                    int val1 = sub.getSubCategoryID();
                    int val2 = subCategoryID;

                    if (val1 == val2){
                        sub.setSubCategoryName(newEnt.getSubCategoryName());
                        break;
                    }
                }
            }

            return new ResponseEntity(newEnt,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("SubCategoryController updateSubCategory ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/subcategories/{subCategoryID}")
    public ResponseEntity deleteSubCategory(@PathVariable(value = "subCategoryID") Integer subCategoryID){

        try{
            Optional<SubCategory> old = repo.findById(subCategoryID);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            SubCategory delete = null;
            Collection<Category> values = CATEGORY_CACHE.values();

            for (Category entity : values){
                for (SubCategory sub:entity.getSubCategories()){
                    int val1 = sub.getSubCategoryID();
                    int val2 = subCategoryID;

                    if (val1 == val2){
                        delete = sub;
                        break;
                    }
                }

                if (delete != null){
                    entity.getSubCategories().remove(delete);
                    break;
                }

            }

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("SubCategoryController deleteSubCategory ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
