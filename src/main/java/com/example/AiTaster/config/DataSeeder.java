package com.example.AiTaster.config;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.UserRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.Console;

@Component
//Thấy @Component ở DataSeeder -> Spring tạo object DataSeeder thành bean để spring quản lý


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner { //CommandLineRunner
//→ giúp class này tự chạy sau khi app start
    UserRepo userRepo;


//→ trì hoãn tạo bean, thường dùng để tránh lỗi vòng phụ thuộc hoặc chỉ tạo khi cần.
    //Khi tạo DataSeeder, chưa cần tạo PasswordEncoder ngay.
    //Khi nào gọi passwordEncoder.encode(...) thì Spring mới lấy PasswordEncoder ra dùng.
    @Lazy
    PasswordEncoder passwordEncoder;

    //→ App start xong thì chạy run()
    @Override
    public void run(String... args) throws Exception {
        seedCreateAdminUser();
    }

    private void seedCreateAdminUser() {
        // implement logic to create an admin user if not exists
        if (userRepo.findByUsername("admin").isPresent()) {
            return;
        }

        User admin = User.builder()
                .email("admin@gmail.com")
                .username("admin")
                .passwordHash(passwordEncoder.encode("Admin123@"))
                .userStatus(UserStatus.ACTIVE)
                .role(Role.ADMIN)
                // Các field còn lại không set thì sẽ null
                // fullName = null
                // avatarUrl = null
                // phoneNumber = null
                // gender = null
                .build();

        userRepo.save(admin);

        log.info("Seeded admin user!");

    }
}

//Biến cả class DataSeeder thành 1 bean
//Spring sẽ tự tạo object DataSeeder và quản lý nó


//Đây là class cấu hình.
//Bên trong có các hàm @Bean.
//Mỗi hàm @Bean sẽ tạo ra 1 bean riêng cho Spring quản lý.