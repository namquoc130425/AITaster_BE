package com.example.AiTaster.repository;

import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

   Optional<User>  findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

}
