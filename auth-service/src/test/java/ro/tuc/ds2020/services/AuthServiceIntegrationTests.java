package ro.tuc.ds2020.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.tuc.ds2020.AuthServiceTestConfig;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.DuplicateResourceException;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.UnauthorizedException;
import ro.tuc.ds2020.dtos.LoginRequestDTO;
import ro.tuc.ds2020.dtos.LoginResponseDTO;
import ro.tuc.ds2020.dtos.RegisterRequestDTO;
import ro.tuc.ds2020.dtos.RegisterResponseDTO;
import ro.tuc.ds2020.dtos.ValidateResponseDTO;
import ro.tuc.ds2020.entities.UserRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthServiceIntegrationTests extends AuthServiceTestConfig {

    @Autowired
    private AuthService authService;

    @Test
    public void testRegisterAndLoginIntegration() {
        String username = "integration_test_user";
        String password = "password123";

        RegisterRequestDTO registerDTO = new RegisterRequestDTO(username, password, "Integration User", UserRole.CLIENT);
        RegisterResponseDTO registered = authService.register(registerDTO);

        assertNotNull(registered.getId());
        assertEquals(username, registered.getUsername());
        assertEquals(UserRole.CLIENT, registered.getRole());

        LoginRequestDTO loginDTO = new LoginRequestDTO(username, password);
        LoginResponseDTO loginResponse = authService.login(loginDTO);

        assertNotNull(loginResponse.getToken());
        assertEquals(username, loginResponse.getUsername());
        assertEquals("CLIENT", loginResponse.getRole());

        ValidateResponseDTO validateResponse = authService.validateToken("Bearer " + loginResponse.getToken());
        assertTrue(validateResponse.isValid());
        assertEquals(username, validateResponse.getUsername());
        assertEquals("CLIENT", validateResponse.getRole());
    }

    @Test
    public void testLoginWithInvalidPasswordThrowsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> {
            authService.login(new LoginRequestDTO("admin", "wrongpassword"));
        });
    }

    @Test
    public void testDuplicateRegistrationThrowsDuplicateResource() {
        RegisterRequestDTO dto1 = new RegisterRequestDTO("duplicate_user", "pass", "Name", UserRole.CLIENT);
        authService.register(dto1);

        RegisterRequestDTO dto2 = new RegisterRequestDTO("duplicate_user", "pass2", "Name", UserRole.CLIENT);
        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(dto2);
        });
    }
}
