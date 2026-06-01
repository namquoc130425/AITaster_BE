package com.example.AiTaster.repository;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRole(Role role);

    List<User> findByUserStatus(UserStatus userStatus);

    List<User> findByRoleAndUserStatus(
            Role role,
            UserStatus userStatus
    );

    List<User> findByFullNameContainingIgnoreCase(String keyword);

}
