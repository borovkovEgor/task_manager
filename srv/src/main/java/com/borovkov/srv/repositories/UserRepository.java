package com.borovkov.srv.repositories;

import com.borovkov.srv.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select s from User s where s.username=:username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("select s.id from User s where s.username ilike lower(concat('%', :username, '%'))")
    List<Long> findByUsernameIgnoreCaseContaining(@Param("username") String username);

    @Query("select s from User s where s.createdBy=:adminId")
    List<User> findUsersByAdminId(@Param("adminId") Long adminId);
}
