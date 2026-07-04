package com.example.AiTaster.repository;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRole(Role role);

    List<User> findByUserStatus(UserStatus userStatus);

    List<User> findByRoleAndUserStatus(
            Role role,
            UserStatus userStatus
    );

    List<User> findByFullNameContainingIgnoreCase(String keyword);

    long countByRole(Role role);

    long countByUserStatus(UserStatus userStatus);

    List<User> findByRoleInAndCreateAtBetween(
            List<Role> roles,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countByRoleAndCreateAtBefore(
            Role role,
            LocalDateTime dateTime
    );

    List<User> findByCreateAtAfterOrderByCreateAtDesc(
            LocalDateTime createdAfter,
            Pageable pageable
    );
}
