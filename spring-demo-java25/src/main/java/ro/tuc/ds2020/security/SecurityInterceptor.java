package ro.tuc.ds2020.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.ForbiddenException;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.UnauthorizedException;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);

    private final JwtUtil jwtUtil;

    @Autowired
    public SecurityInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod().toUpperCase();
        String path = request.getServletPath();

        // Allow CORS pre-flight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow public paths and documentation
        if (isPublicPath(path)) {
            return true;
        }

        // Allow internal microservice synchronization calls
        String internalHeader = request.getHeader("X-Internal-Call");
        if ("true".equalsIgnoreCase(internalHeader)) {
            LOGGER.debug("Bypassing auth for internal microservice call: {} {}", method, path);
            return true;
        }

        // Check for ForwardAuth headers (set by Traefik) or validate Bearer JWT token directly
        String role = request.getHeader("X-User-Role");
        String userId = request.getHeader("X-User-Id");

        if (role == null || role.isBlank()) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || authHeader.isBlank()) {
                LOGGER.warn("Access denied: Missing Authorization header on {} {}", method, path);
                throw new UnauthorizedException("Missing Authorization header");
            }

            JwtClaims claims = jwtUtil.validateToken(authHeader);
            if (claims == null) {
                LOGGER.warn("Access denied: Invalid or expired JWT token on {} {}", method, path);
                throw new UnauthorizedException("Invalid or expired JWT token");
            }

            role = claims.getRole();
            userId = claims.getUserId() != null ? claims.getUserId().toString() : null;
        }

        // Store identity in request attributes
        request.setAttribute("authRole", role);
        request.setAttribute("authUserId", userId);

        // RBAC Enforcement
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }

        if ("CLIENT".equalsIgnoreCase(role)) {
            // Client is ONLY permitted to view their own profile (GET /user/{userId})
            if ("GET".equalsIgnoreCase(method) && isClientOwnProfile(path, userId)) {
                return true;
            }
            LOGGER.warn("CLIENT access denied: method={} path={} userId={}", method, path, userId);
            throw new ForbiddenException("CLIENT role is only allowed to view their own profile");
        }

        LOGGER.warn("Unknown or unauthorized role '{}' accessing {} {}", role, method, path);
        throw new ForbiddenException("Insufficient privileges");
    }

    private boolean isPublicPath(String path) {
        return path == null
                || "/".equals(path)
                || path.startsWith("/error")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }

    private boolean isClientOwnProfile(String path, String userId) {
        if (userId == null || path == null) {
            return false;
        }
        String prefix = "/user/";
        if (!path.startsWith(prefix)) {
            return false;
        }
        String requestedId = path.substring(prefix.length());
        return userId.equalsIgnoreCase(requestedId);
    }
}
