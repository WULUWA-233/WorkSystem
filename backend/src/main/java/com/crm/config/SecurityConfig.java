package com.crm.config;

import com.crm.common.Result;
import com.crm.common.ResultCode;
import com.crm.mapper.SysRoleMapper;
import com.crm.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security Configuration
 * JWT authentication is handled by {@link JwtAuthenticationFilter}.
 *
 * @author CRM Team
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;

    public SecurityConfig(SysRoleMapper sysRoleMapper, SysUserMapper sysUserMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(sysRoleMapper, sysUserMapper);
    }

    /** Run this filter only inside Spring Security's configured filter chain. */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for REST API
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers(
                            "/api/v1/common/health",
                            "/api/v1/auth/login",
                            "/doc.html",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/v2/api-docs/**",
                            "/v3/api-docs/**",
                            "/webjars/**",
                            "/favicon.ico",
                            "/error"
                    ).permitAll()
                    .antMatchers("/api/v1/system/operation-logs/**").hasAnyAuthority("ADMIN", "AUDITOR")
                    .antMatchers("/api/v1/system/**").hasAuthority("ADMIN")
                    .antMatchers(
                            "/api/v1/customer/**",
                            "/api/v1/sales/**",
                            "/api/v1/analytics/**",
                            "/api/v1/ai/**"
                    ).hasAnyAuthority("ADMIN", "SALES", "MANAGER", "AUDITOR")
                    .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(
                                Result.error(ResultCode.UNAUTHORIZED, "Not logged in or token invalid")));
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(
                                Result.error(ResultCode.FORBIDDEN, "No permission to access")));
                    })
                .and()
                // Stateless session
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // Disable form login and HTTP basic
                .formLogin().disable()
                .httpBasic().disable()
                // Register JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
