package com.zhongzhuan.proxy.config;

import com.zhongzhuan.proxy.repository.AdminUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminUserRepository adminUserRepository) {
        return username -> adminUserRepository.findByUsername(username)
                .map(adminUser -> User.withUsername(adminUser.getUsername())
                        .password(adminUser.getPassword())
                        .roles(adminUser.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found: " + username));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                // Relay API - open to public (our custom auth filter handles it)
                .requestMatchers("/api/proxy/**").permitAll()
                // Public API for users (routes info)
                .requestMatchers("/api/public/**").permitAll()
                // Admin init endpoint must be accessible without auth
                .requestMatchers(HttpMethod.POST, "/api/admin/init").permitAll()
                // User registration & login
                .requestMatchers("/api/auth/**").permitAll()
                // Admin API requires authentication
                .requestMatchers("/api/admin/**").authenticated()
                // Dev tools
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
