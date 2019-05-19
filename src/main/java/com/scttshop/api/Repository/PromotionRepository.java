package com.scttshop.api.Repository;

import com.scttshop.api.Entity.Product;
import com.scttshop.api.Entity.Promotion;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface PromotionRepository extends JpaRepository<Promotion,Integer> {

        @Cacheable(value="promotions",key="#type + #isActive")
        List<Promotion> findByTypeAndIsActiveOrderByAppliedID(String type,Integer isActive);

        @Cacheable(value="promotions",key="#type + #isActive + #id")
        Promotion findByTypeAndAppliedIDAndIsActive(String type,Integer id,Integer isActive);

        @Query("SELECT p.productName from Product p JOIN Promotion s ON p.productID = s.appliedID WHERE s.type = 'PRODUCT' AND p.productID = ?1")
        @Cacheable(value="promotions")
        String getAppliedName(Integer appliedID);
}
