package com.lorenzodm.librepm.security;

import com.lorenzodm.librepm.api.exception.ForbiddenException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/** Prevents authenticated clients from impersonating a different URL/header user. */
@Configuration
public class AuthenticatedIdentityConfig implements WebMvcConfigurer {
    private final AuthProperties auth;
    private final com.lorenzodm.librepm.service.PermissionChecker permissions;
    public AuthenticatedIdentityConfig(AuthProperties auth, com.lorenzodm.librepm.service.PermissionChecker permissions) {
        this.auth = auth;
        this.permissions = permissions;
    }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (!auth.isEnabled()) return true;
                var identity = CurrentUserUtils.getUserIdOrNull();
                Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                String userId = variables instanceof Map<?, ?> map ? (String) map.get("userId") : null;
                String header = request.getHeader("X-User-Id");
                if ((userId != null && !identity.filter(userId::equals).isPresent()) ||
                        (header != null && !identity.filter(header::equals).isPresent())) {
                    throw new ForbiddenException("Authenticated identity does not match requested user");
                }
                String projectId = variables instanceof Map<?, ?> map ? (String) map.get("projectId") : null;
                if (projectId != null) {
                    String actor = identity.orElseThrow(() -> new ForbiddenException("Authenticated identity required"));
                    boolean reading = java.util.Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod());
                    permissions.requirePermission(actor, projectId, reading ? "PROJECT_READ" : "PROJECT_WRITE");
                }
                return true;
            }
        }).addPathPatterns("/api/**");
    }
}
