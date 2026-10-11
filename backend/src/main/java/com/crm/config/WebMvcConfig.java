package com.crm.config;

import com.crm.interceptor.AdminInterceptor;
import com.crm.interceptor.AuditorReadOnlyInterceptor;
import com.crm.interceptor.CustomerPermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration — registers interceptors
 *
 * @author CRM Team
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;
    private final CustomerPermissionInterceptor customerPermissionInterceptor;
    private final AuditorReadOnlyInterceptor auditorReadOnlyInterceptor;

    public WebMvcConfig(AdminInterceptor adminInterceptor,
                        CustomerPermissionInterceptor customerPermissionInterceptor,
                        AuditorReadOnlyInterceptor auditorReadOnlyInterceptor) {
        this.adminInterceptor = adminInterceptor;
        this.customerPermissionInterceptor = customerPermissionInterceptor;
        this.auditorReadOnlyInterceptor = auditorReadOnlyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ADMIN-only: system management
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/v1/system/users/**",
                                 "/api/v1/system/roles/**");

        // ADMIN, SALES, MANAGER, or AUDITOR: business modules.
        registry.addInterceptor(customerPermissionInterceptor)
                .addPathPatterns("/api/v1/customer/customers/**",
                                 "/api/v1/customer/follow-records/**",
                                 "/api/v1/sales/opportunities/**",
                                 "/api/v1/sales/orders/**",
                                 "/api/v1/analytics/**");

        // AUDITOR is read-only for business modules and operation logs.
        registry.addInterceptor(auditorReadOnlyInterceptor)
                .addPathPatterns("/api/v1/customer/customers/**",
                                 "/api/v1/customer/follow-records/**",
                                 "/api/v1/sales/opportunities/**",
                                 "/api/v1/sales/orders/**",
                                 "/api/v1/analytics/**",
                                 "/api/v1/system/operation-logs/**");
    }
}
