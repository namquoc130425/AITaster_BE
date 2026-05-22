package com.example.AiTaster.entity;

import com.example.AiTaster.constant.Gender;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter // tạo gettter tự động
@Setter // tạo setter tự động
@AllArgsConstructor // tạo constructor có tham số
@NoArgsConstructor // tạo constructor ko thanm số
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements UserDetails {
    @Id //Đánh giấu đây là khóa chính của đối tượng
    @GeneratedValue(strategy = GenerationType.IDENTITY) //khi tạo đối tượng sẽ tự động tạo khóa chính
    Long id; //là khóa chính của đối tượng (User)

    String name;

    @Column(unique = true)
    String username;

    String password;

    @Enumerated(EnumType.STRING)
    Role role;

    @Enumerated(EnumType.STRING)
    UserStatus userStatus;

    int age;
    String address;

    @Column(unique = true) // Lưu xuống DB 1 giá trị duy nhất
    String phone;

    @Enumerated(EnumType.STRING) // lưu xuongs db sẽ là dạng chữ thay vì số
    Gender gender;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of( new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.username;
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
