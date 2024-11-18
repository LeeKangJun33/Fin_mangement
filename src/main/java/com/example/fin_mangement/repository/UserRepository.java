package com.example.fin_mangement.repository;


import com.example.fin_mangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

   Optional<User> findByUsername(String username);

   boolean existsByEmail(String email);
}
