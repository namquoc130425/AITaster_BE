package com.example.AiTaster.repository;

import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    User findByUsername(String userName);

    boolean existsByUsername(String userName);

    boolean existsByPhone(String phone);

}
