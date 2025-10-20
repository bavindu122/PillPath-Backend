package com.leo.pillpathbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy;
import org.springframework.http.HttpMethod;

import com.leo.pillpathbackend.security.filter.CustomTokenAuthenticationFilter;
import org.springframework.security.web.header.writers.CrossOriginResourcePolicyHeaderWriter.CrossOriginResourcePolicy;

import java.util.List;

import com.leo.pillpathbackend.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    public SecurityConfig() {
        System.out.println("🟢🟢🟢 SecurityConfig LOADED! 🟢🟢🟢");
    }

    @Autowired
    private CustomTokenAuthenticationFilter customTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        System.out.println("🔧 Building SecurityFilterChain...");
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(customTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
      
                .headers(headers -> headers
                        .crossOriginOpenerPolicy(coop -> coop.policy(CrossOriginOpenerPolicy.SAME_ORIGIN_ALLOW_POPUPS))
                        .crossOriginResourcePolicy(corp -> corp.policy(CrossOriginResourcePolicy.CROSS_ORIGIN))
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/app/**").permitAll()
                        .requestMatchers("/topic/**").permitAll()
                        .requestMatchers("/ws/health").permitAll()
                        .requestMatchers("/ws/chat/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Auth endpoints
                        .requestMatchers("/api/v1/users/**").permitAll()
                        .requestMatchers("/api/v1/users/admin/login").permitAll()
                        .requestMatchers("/api/v1/users/change-password").permitAll()
                        .requestMatchers("/api/v1/customers/register").permitAll()
                        .requestMatchers("/api/v1/customers/login").permitAll()
                        .requestMatchers("/api/v1/customers/oauth").permitAll()
                        .requestMatchers("/api/v1/customers/check-email/**").permitAll()
                        .requestMatchers("/api/v1/customers/profile/**").permitAll()
                        // Public registration endpoints
                        .requestMatchers("/api/v1/pharmacies/register").permitAll()
                        .requestMatchers("/api/v1/pharmacy-admins/register").permitAll()

                        // Protect admin and pharmacy-admin APIs
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/pharmacy-admin/**").hasAuthority("PHARMACY_ADMIN")
                        .requestMatchers("/api/pharmacy-admin/**").hasAuthority("PHARMACY_ADMIN")

                        // Leave existing public APIs as-is for now
                        .requestMatchers("/api/v1/pharmacies/**").permitAll()
                        .requestMatchers("/api/v1/prescriptions/**").permitAll()
                        .requestMatchers("/api/v1/medicines/**").permitAll()
                        .requestMatchers("/api/v1/orders/**").permitAll()
                        .requestMatchers("/api/v1/notifications/**").permitAll()
                        .requestMatchers("/api/v1/wallets/**").permitAll()
                        .requestMatchers("/api/chats/**").permitAll()  // Chat endpoints
                        .requestMatchers("/api/v1/chats/**").permitAll()  // Chat endpoints with v1
                        .requestMatchers("/api/v1/v1/chats/**").permitAll()  // Temporary alias path used by frontend
                        .requestMatchers("/api/v1/v1/chats/threads").permitAll()  // Explicit path
                        .requestMatchers(HttpMethod.GET, "/api/v1/chats/*/messages").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/chats/unread-count").permitAll()
                        .requestMatchers("/api/v1/notifications/**").permitAll()
                        .requestMatchers("/api/otc-orders/**").permitAll()
                        .requestMatchers("/api/pharmacy-orders/**").permitAll()
                        .requestMatchers("/api/v1/pharmacy/dashboard/**").permitAll()

                        .anyRequest().authenticated()
                );

        System.out.println("✅ SecurityFilterChain built successfully!");
        System.out.println("✅ /api/otc-orders/** is set to .permitAll()");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Single, consistent CORS configuration (no duplicate variables)
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true); // Explicit origins above keep this valid

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}