package com.DoAn.Web_QLDH_DichVu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Cấu hình thuật toán mã hóa mật khẩu (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình phân quyền đường dẫn và form đăng nhập
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Những đường dẫn ai cũng vào được
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                        // Mọi đường dẫn khác đều yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Trỏ tới URL trang đăng nhập của mình
                        .defaultSuccessUrl("/", true) // Đăng nhập xong quay về trang chủ
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // Đăng xuất xong quay về trang login
                        .permitAll()
                );

        return http.build();
    }
}