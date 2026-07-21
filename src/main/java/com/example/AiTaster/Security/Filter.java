package com.example.AiTaster.Security;



import com.example.AiTaster.config.SecurityProperties;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
@Slf4j
@Component
public class Filter extends OncePerRequestFilter {

@Autowired
TokenService tokenService;

@Autowired
    SecurityProperties securityProperties;

@Autowired
@Qualifier("handlerExceptionResolver")
HandlerExceptionResolver handlerException;

// AntPathMatcher kiểm tra URL có khớp với publicEndpoints không.
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    // Kiểm tra có phải API công khai không.
    private boolean isPulicEndpoints(String url) {
        List<String> publicEndpoints = securityProperties.getPublicEndpoints();

        if(publicEndpoints == null || publicEndpoints.isEmpty()) {
            return false;
        }
        return publicEndpoints.stream().anyMatch(pattern -> antPathMatcher.match(pattern, url));
    }

    // Lấy token từ yêu cầu.
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       String path = request.getRequestURI();                 // lấy đường dẫn request
       String token = getTokenFromRequest(request);           // lấy token từ request

       // Kiểm tra API công khai.
        if(isPulicEndpoints(path)) {
             filterChain.doFilter(request, response);        // nếu dường dẫn công khai thì bỏ qua filter và tiếp tục đi tới filter tiếp theo
             return;
        }
        if(token != null) {
            try{
<<<<<<< HEAD
               User user = tokenService.verifyAccessToken(token);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
=======
               User user = tokenService.verifyAccessToken(token);         // kiểm tra token từ người dùng
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());  // tạo 1 đối tượng Authentication để biết ai gữi request ,đăng nhập chưa,role hoặc permission gì (phần quyền)
                SecurityContextHolder.getContext().setAuthentication(authenticationToken); // sau này có thể lấy user đang đăng nhập bằng cách gọi SecurityContextHolder.getContext().getAuthentication().getPrincipal()
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
            } catch (GlobalException e) {
                SecurityContextHolder.clearContext();
                handlerExceptionResolver.resolveException(request, response, null, e);
                return;
            }
        }
        filterChain.doFilter(request, response);

    }

}
