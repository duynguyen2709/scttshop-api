package com.scttshop.api.Repository;

import com.scttshop.api.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author duyna5
 */

@Repository
@Transactional
public interface CommentRepository extends JpaRepository<Comment,Integer> {
}
