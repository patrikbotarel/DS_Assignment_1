package ro.tuc.ds2020.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.ForbiddenException;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.UnauthorizedException;
import ro.tuc.ds2020.dtos.LoginRequestDTO;
import ro.tuc.ds2020.dtos.LoginResponseDTO;
import ro.tuc.ds2020.dtos.RegisterRequestDTO;
import ro.tuc.ds2020.dtos.RegisterResponseDTO;
import ro.tuc.ds2020.dtos.UserSyncDTO;
import ro.tuc.ds2020.dtos.ValidateResponseDTO;
import ro.tuc.ds2020.services.AuthService;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping(value = "/auth")
@Tag(name = "Authentication & Authorization", description = "Endpoints for user login, JWT generation, registration, and token validation")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Authenticate user and issue JWT token",
            description = "Validates username and password credentials against Credential Database. Returns a signed JWT token on success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation error on credentials payload"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Register new user credentials",
            description = "Registers user credentials into the Credential Database and notifies User Service to create the user profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Validation error on registration payload"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping(value = "/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        RegisterResponseDTO response = authService.register(registerDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Validate JWT token",
            description = "Validates the Bearer JWT token from the Authorization header. Used by Traefik ForwardAuth reverse-proxy and clients.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid and active"),
            @ApiResponse(responseCode = "401", description = "Token is missing, expired, or signature is invalid"),
            @ApiResponse(responseCode = "403", description = "User does not have permission for the requested resource")
    })
    @RequestMapping(value = "/validate", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ValidateResponseDTO> validateToken(
            @Parameter(description = "Bearer JWT token")
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Parameter(description = "Forwarded URI from Traefik reverse-proxy")
            @RequestHeader(value = "X-Forwarded-Uri", required = false) String forwardedUri,
            @Parameter(description = "Forwarded HTTP Method from Traefik reverse-proxy")
            @RequestHeader(value = "X-Forwarded-Method", required = false) String forwardedMethod) {

        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedException("Missing Authorization header");
        }

        ValidateResponseDTO response = authService.validateToken(authHeader);

        // RBAC check for Traefik ForwardAuth if forwarded headers are present
        if (forwardedUri != null && !forwardedUri.isBlank()) {
            enforceRbacPermissions(response, forwardedUri, forwardedMethod);
        }

        HttpHeaders headers = new HttpHeaders();
        if (response.getUserId() != null) {
            headers.set("X-User-Id", response.getUserId().toString());
        }
        headers.set("X-User-Role", response.getRole());
        headers.set("X-User-Name", response.getUsername());

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @Operation(summary = "Internal sync endpoint: Register or update user credentials from User Microservice",
            description = "Saves user credentials directly to auth database upon user creation in user-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credentials successfully synchronized"),
            @ApiResponse(responseCode = "400", description = "Invalid sync payload")
    })
    @PostMapping(value = {"/sync/user", "/register/internal"})
    public ResponseEntity<Void> syncUser(
            @Parameter(description = "User credentials and metadata to synchronize", required = true)
            @RequestBody final UserSyncDTO syncDTO) {
        authService.syncUser(syncDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Internal sync endpoint: Delete user credentials from User Microservice",
            description = "Deletes user credentials upon user deletion in user-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Credentials successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User credential record not found")
    })
    @DeleteMapping(value = "/sync/user/{id}")
    public ResponseEntity<Void> deleteSyncUser(
            @Parameter(description = "UUID of the user whose credentials should be removed", required = true)
            @PathVariable("id") final UUID id) {
        authService.deleteSyncUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private void enforceRbacPermissions(ValidateResponseDTO user, String uri, String method) {
        String role = user.getRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            return;
        }

        String normalizedMethod = method != null ? method.toUpperCase() : "GET";
        String normalizedUri = uri.toLowerCase();
        int queryIdx = normalizedUri.indexOf('?');
        if (queryIdx != -1) {
            normalizedUri = normalizedUri.substring(0, queryIdx);
        }
        if (normalizedUri.endsWith("/") && normalizedUri.length() > 1) {
            normalizedUri = normalizedUri.substring(0, normalizedUri.length() - 1);
        }

        // CLIENT role restrictions
        if (normalizedUri.startsWith("/user")) {
            // Client can only view their own user details: GET /user/{userId}
            if (!"GET".equals(normalizedMethod)) {
                LOGGER.warn("CLIENT user '{}' denied {} access to {}", user.getUsername(), method, uri);
                throw new ForbiddenException("CLIENT role cannot perform " + method + " on users");
            }
            if (user.getUserId() == null || !normalizedUri.equals("/user/" + user.getUserId().toString().toLowerCase())) {
                LOGGER.warn("CLIENT user '{}' attempted to access unauthorized user path {}", user.getUsername(), uri);
                throw new ForbiddenException("CLIENT can only access their own profile");
            }
        } else if (normalizedUri.startsWith("/device")) {
            // Client can only view their own assigned devices: GET /device/user/{userId}
            if (!"GET".equals(normalizedMethod)) {
                LOGGER.warn("CLIENT user '{}' denied {} access to devices", user.getUsername(), method);
                throw new ForbiddenException("CLIENT role cannot perform " + method + " on devices");
            }
            if (user.getUserId() == null || !normalizedUri.equals("/device/user/" + user.getUserId().toString().toLowerCase())) {
                LOGGER.warn("CLIENT user '{}' attempted to access unauthorized device path {}", user.getUsername(), uri);
                throw new ForbiddenException("CLIENT can only view their own assigned devices");
            }
        }
    }
}
