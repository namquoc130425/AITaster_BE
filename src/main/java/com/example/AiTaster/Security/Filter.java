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
       String path = request.getRequestURI();
       String token = getTokenFromRequest(request);

       // Kiểm tra API công khai.
        if(isPulicEndpoints(path)) {
             filterChain.doFilter(request, response);
             return;
        }
        if(token != null) {
            try{
               User user = tokenService.verifyAccessToken(token);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (GlobalException e) {
                SecurityContextHolder.clearContext();
                handlerExceptionResolver.resolveException(request, response, null, e);
                return;
            }
        }
        filterChain.doFilter(request, response);

    }

}
