package ro.tuc.ds2020.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.DuplicateResourceException;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.UnauthorizedException;
import ro.tuc.ds2020.dtos.LoginRequestDTO;
import ro.tuc.ds2020.dtos.LoginResponseDTO;
import ro.tuc.ds2020.dtos.RegisterRequestDTO;
import ro.tuc.ds2020.dtos.RegisterResponseDTO;
import ro.tuc.ds2020.dtos.UserSyncDTO;
import ro.tuc.ds2020.dtos.ValidateResponseDTO;
import ro.tuc.ds2020.entities.UserCredential;
import ro.tuc.ds2020.entities.UserRole;
import ro.tuc.ds2020.repositories.UserCredentialRepository;
import ro.tuc.ds2020.security.JwtClaims;
import ro.tuc.ds2020.security.JwtUtil;
import ro.tuc.ds2020.services.client.UserServiceClient;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserServiceClient userServiceClient;

    @Autowired
    public AuthService(UserCredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       UserServiceClient userServiceClient) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userServiceClient = userServiceClient;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        LOGGER.info("Processing login request for username: {}", loginRequest.getUsername());

        UserCredential credential = credentialRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    LOGGER.warn("Login failed: username '{}' not found", loginRequest.getUsername());
                    return new UnauthorizedException("Invalid username or password");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), credential.getPassword())) {
            LOGGER.warn("Login failed: incorrect password for username '{}'", loginRequest.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }

        String role = credential.getRole() != null ? credential.getRole().name() : "CLIENT";
        String token = jwtUtil.generateToken(credential.getId(), credential.getUsername(), role);

        LOGGER.info("User '{}' successfully authenticated with role '{}'", credential.getUsername(), role);
        return new LoginResponseDTO(token, credential.getId(), credential.getUsername(), role, jwtUtil.getExpirationInSeconds());
    }

    public RegisterResponseDTO register(RegisterRequestDTO registerDTO) {
        LOGGER.info("Processing registration for username: {}", registerDTO.getUsername());

        if (credentialRepository.existsByUsername(registerDTO.getUsername())) {
            LOGGER.warn("Registration rejected: username '{}' already taken", registerDTO.getUsername());
            throw new DuplicateResourceException("Username '" + registerDTO.getUsername() + "' is already registered");
        }

        UserRole role = registerDTO.getRole() != null ? registerDTO.getRole() : UserRole.CLIENT;
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());

        UserCredential credential = new UserCredential(registerDTO.getUsername(), encodedPassword, role);
        credential = credentialRepository.save(credential);
        LOGGER.info("Created credential entry with ID {} for user '{}'", credential.getId(), credential.getUsername());

        // Synchronize with User Microservice so profile exists with same ID
        userServiceClient.synchronizeUserCreation(credential.getId(), registerDTO);

        return new RegisterResponseDTO(
                credential.getId(),
                credential.getUsername(),
                credential.getRole(),
                "User successfully registered"
        );
    }

    public ValidateResponseDTO validateToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            LOGGER.warn("Token validation failed: missing Authorization header");
            throw new UnauthorizedException("Missing or empty Authorization header");
        }

        JwtClaims claims = jwtUtil.validateToken(authHeader);
        if (claims == null) {
            LOGGER.warn("Token validation failed: invalid signature, expired token, or malformed JWT");
            throw new UnauthorizedException("Invalid or expired JWT token");
        }

        return new ValidateResponseDTO(true, claims.getUserId(), claims.getUsername(), claims.getRole());
    }

    @Transactional
    public void syncUser(final UserSyncDTO syncDTO) {
        LOGGER.info("Syncing user credential from User Service: id={}, username={}", syncDTO.getId(), syncDTO.getUsername());

        Optional<UserCredential> existing = Optional.empty();
        if (syncDTO.getId() != null) {
            existing = credentialRepository.findById(syncDTO.getId());
        }
        if (existing.isEmpty() && syncDTO.getUsername() != null) {
            existing = credentialRepository.findByUsername(syncDTO.getUsername());
        }

        String encodedPassword = syncDTO.getPassword() != null && !syncDTO.getPassword().startsWith("$2a$")
                ? passwordEncoder.encode(syncDTO.getPassword())
                : syncDTO.getPassword();

        UserRole role = syncDTO.getRole() != null ? syncDTO.getRole() : UserRole.CLIENT;

        if (existing.isPresent()) {
            UserCredential credential = existing.get();
            credential.setUsername(syncDTO.getUsername());
            if (encodedPassword != null && !encodedPassword.isBlank()) {
                credential.setPassword(encodedPassword);
            }
            credential.setRole(role);
            credential.setNewRecord(false);
            credentialRepository.save(credential);
            LOGGER.info("Updated credential for username '{}'", credential.getUsername());
        } else {
            UUID assignedId = syncDTO.getId() != null ? syncDTO.getId() : UUID.randomUUID();
            UserCredential credential = new UserCredential(
                    assignedId,
                    syncDTO.getUsername(),
                    encodedPassword,
                    role
            );
            credential.setNewRecord(true);
            credentialRepository.save(credential);
            LOGGER.info("Saved new credential with ID {} from sync for username '{}'", assignedId, credential.getUsername());
        }
    }

    @Transactional
    public void deleteSyncUser(final UUID id) {
        LOGGER.info("Syncing user deletion for id: {}", id);
        if (credentialRepository.existsById(id)) {
            credentialRepository.deleteById(id);
            LOGGER.info("Deleted credential with id: {}", id);
        } else {
            LOGGER.info("User with id {} not found in auth_db, nothing to delete", id);
        }
    }
}
