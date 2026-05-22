package com.example.AiTaster.service;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.LoginRequest;
import com.example.AiTaster.dto.request.RegisterRequest;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService implements UserDetailsService, IAuthentication {

    @Autowired
    UserRepo userRepo;
    @Autowired
    UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;

    public UserResponse register(RegisterRequest registerRequest) {
        // kiểm tra username và Phone đã tồn tại hay chưa ( validation ) : existsBy.....
        if (userRepo.existsByPhone(registerRequest.getPhone())) {
            // xữ lý lỗi nếu phone tồn tại
            throw new GlobalException(400, "Duplicate phone number");
        }

        if (userRepo.existsByUsername(registerRequest.getUsername()))
            {
                // xữ lý lỗi nếu name tồn tại
                throw new GlobalException(400, "Duplicate UserName ");
            }

            //mapper từ request sang entity
            User entity = userMapper.toEntity(registerRequest);
            entity.setRole(Role.CUSTOMER);
            entity.setUserStatus(UserStatus.ACTIVE);

            // mã hóa password trước khi lưu vào database
            entity.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            // lưu xuống database
            User user = userRepo.save(entity);
            //chuyển qua userResponse
            UserResponse response = userMapper.toResponser(user);

            return response;


    }
    @Override
    public UserResponse login(LoginRequest loginRequest) {
        try{
            // xác thực bằng Spring security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            // ép kiểu từ Authen -) User
            User user = (User) authentication.getPrincipal();
            //generate token access token
            String accessToken = tokenService.generateAccessToken(user);
            // trả về cho FE
            UserResponse response = userMapper.toResponser(user);
            response.setAccessToken(accessToken);
            return response;

        }
        catch (LockedException e) {
            throw new GlobalException(403,"Account is locked");
        }
        catch (DisabledException e) {
            throw new GlobalException(403,"Account is disabled");
        }
        catch (BadCredentialsException e) {
            throw new GlobalException(400,"Invalid username or password");
        }


        catch (Exception e) {
            log.info(e.getMessage());
            throw new GlobalException(e.getMessage());
        }

    }

    //lấy username
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(userName);

        if(user == null){
            throw new UsernameNotFoundException(
                    "User not found: " + userName
            );
        }

        return user;
    }


}
