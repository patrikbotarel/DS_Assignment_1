package ro.tuc.ds2020.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.tuc.ds2020.AuthServiceTestConfig;
import ro.tuc.ds2020.dtos.LoginRequestDTO;
import ro.tuc.ds2020.dtos.LoginResponseDTO;
import ro.tuc.ds2020.dtos.RegisterRequestDTO;
import ro.tuc.ds2020.dtos.RegisterResponseDTO;
import ro.tuc.ds2020.dtos.ValidateResponseDTO;
import ro.tuc.ds2020.entities.UserRole;
import ro.tuc.ds2020.services.AuthService;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerUnitTest extends AuthServiceTestConfig {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    public void loginSuccessTest() throws Exception {
        UUID userId = UUID.randomUUID();
        LoginRequestDTO request = new LoginRequestDTO("admin", "admin123");
        LoginResponseDTO response = new LoginResponseDTO("mock.jwt.token", userId, "admin", "ADMIN", 86400L);

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .content(jsonMapper.writeValueAsString(request))
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    public void loginFailsDueToBlankUsernameTest() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("", "admin123");

        mockMvc.perform(post("/auth/login")
                .content(jsonMapper.writeValueAsString(request))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerSuccessTest() throws Exception {
        UUID userId = UUID.randomUUID();
        RegisterRequestDTO request = new RegisterRequestDTO("newclient", "secret123", "New Client", UserRole.CLIENT);
        RegisterResponseDTO response = new RegisterResponseDTO(userId, "newclient", UserRole.CLIENT, "User successfully registered");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                .content(jsonMapper.writeValueAsString(request))
                .contentType("application/json"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newclient"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    public void validateTokenSuccessTest() throws Exception {
        UUID userId = UUID.randomUUID();
        ValidateResponseDTO response = new ValidateResponseDTO(true, userId, "admin", "ADMIN");

        when(authService.validateToken("Bearer valid.jwt.token")).thenReturn(response);

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-User-Role", "ADMIN"))
                .andExpect(header().string("X-User-Name", "admin"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    public void validateTokenFailsWhenMissingHeaderTest() throws Exception {
        mockMvc.perform(get("/auth/validate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void validateTokenForbiddenForClientAccessingUserCrudTest() throws Exception {
        UUID clientUserId = UUID.randomUUID();
        ValidateResponseDTO response = new ValidateResponseDTO(true, clientUserId, "client1", "CLIENT");

        when(authService.validateToken("Bearer client.jwt.token")).thenReturn(response);

        // Client attempting to POST (create) a user
        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer client.jwt.token")
                .header("X-Forwarded-Uri", "/user")
                .header("X-Forwarded-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
