package com.example.resolveit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.resolveit.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomSuccessHandler successHandler;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/ws/**", "/auth/register")) // Register is often permitAll, but let's be careful
                .sessionManagement(session -> session
                    .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login", "/register", "/css/**", "/js/**", "/images/**", "/",
                                "/home.html", "/register.html", "/login.html", "/complaint.html", "/form/complaint",
                                "/complaint/**", "/uploads/**", "/ws/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/staff-dashboard/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/profile/**", "/my-complaints/**").hasRole("USER")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/auth/login") // Point to the existing login URL or a new one
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?success=Logged out successfully")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("text/html")) {
                                response.sendRedirect("/login");
                            } else {
                                response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized");
                            }
                        }));

        return http.build();
    }
}
