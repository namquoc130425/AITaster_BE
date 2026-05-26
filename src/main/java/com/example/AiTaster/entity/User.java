package com.example.AiTaster.entity;

import com.example.AiTaster.constant.Gender;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //khi tạo đối tượng sẽ tự động tạo khóa chính
    Long userId;

    @Column(unique = true)
    String email;

    String passwordHash;

    String fullName;

    String avatarUrl;

    @Column(unique = true)
    String phone;

    @Enumerated(EnumType.STRING)
    Role role;

    @Enumerated(EnumType.STRING)
    UserStatus userStatus;
    @CreationTimestamp
   LocalDateTime createAt;
    @UpdateTimestamp
   LocalDateTime updateAt;

    // 1 user có thể liên kết với 1 clientProfile hoặc 1 expertProfile, nhưng không thể có cả hai cùng lúc
    @OneToOne(mappedBy = "user")
    private ClientProfile clientProfile;

    @OneToOne(mappedBy = "user")
    private ExpertProfile expertProfile;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of( new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }



    @Override
    public String getUsername() {
        return this.email;
    }
    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.userStatus != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.userStatus == UserStatus.ACTIVE;
    }

}
