package com.example.resolveit.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.example.resolveit.repository.UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else {
            // Fallback to cookie for standard GET/POST from browser
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    if (username != null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
                        com.example.resolveit.model.User user = userRepository.findByEmail(username);
                        if (user != null) {
                            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.Collections.singletonList(
                                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()));

                            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                            username, null, authorities);
                            
                            auth.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                    return;
                }
            } catch (Exception e) {
                // If token parsing fails, just continue without authentication
                // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token error");
                // return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
