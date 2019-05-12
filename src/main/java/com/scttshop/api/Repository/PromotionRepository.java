package com.scttshop.api.Repository;

import com.scttshop.api.Entity.Product;
import com.scttshop.api.Entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion,Integer> {
        List<Promotion> findByTypeAndIsActive(String type,boolean isActive);

        Promotion findByTypeAndAppliedIDAndIsActive(String type,Integer id,boolean isActive);
}
