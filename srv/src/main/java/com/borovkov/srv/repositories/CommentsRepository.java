package com.borovkov.srv.repositories;

import com.borovkov.srv.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentsRepository extends MongoRepository<Comment, Long> {

    @Query("{ 'taskId': ?0, 'groupId': ?1 }")
    Page<Comment> findByTaskIdAndGroupId(Long taskId, Long groupId, Pageable page);

    @Query("{ '_id': ?0 }")
    Optional<Comment> findById(String id);

    void deleteById(String id);


}
