package com.mcnz.rps.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Cho phép tất cả các yêu cầu
            )
            .csrf(csrf -> csrf.disable()) // Tắt bảo vệ CSRF (chỉ khi thực sự cần)
            .formLogin(form -> form.disable()) // Tắt form đăng nhập
            .rememberMe(remember -> remember
                .tokenValiditySeconds(604800) // Token remember-me có hiệu lực trong 7 ngày
                .key("someSecretKey") // Chìa khóa mã hóa cookie remember-me
            );
        return http.build();
    }
}
