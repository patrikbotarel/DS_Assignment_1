package ro.tuc.ds2020.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.tuc.ds2020.Ds2020TestConfig;
import ro.tuc.ds2020.dtos.UserDTO;
import ro.tuc.ds2020.dtos.UserDetailsDTO;
import ro.tuc.ds2020.entities.UserRole;
import ro.tuc.ds2020.security.SecurityInterceptor;
import ro.tuc.ds2020.services.UserService;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerUnitTest extends Ds2020TestConfig {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private SecurityInterceptor securityInterceptor;

    @BeforeEach
    public void setup() throws Exception {
        when(securityInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    public void insertUserTest() throws Exception {
        UserDetailsDTO userDTO = new UserDetailsDTO("john_doe", "secret123", "John Doe", UserRole.CLIENT);
        when(service.insert(any(UserDetailsDTO.class))).thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/user")
                .content(jsonMapper.writeValueAsString(userDTO))
                .contentType("application/json"))
                .andExpect(status().isCreated());
    }

    @Test
    public void insertUserTestFailsDueToBlankUsername() throws Exception {
        UserDetailsDTO userDTO = new UserDetailsDTO("", "secret123", "John Doe", UserRole.CLIENT);

        mockMvc.perform(post("/user")
                .content(jsonMapper.writeValueAsString(userDTO))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertUserTestFailsDueToBlankPassword() throws Exception {
        UserDetailsDTO userDTO = new UserDetailsDTO("john_doe", "", "John Doe", UserRole.CLIENT);

        mockMvc.perform(post("/user")
                .content(jsonMapper.writeValueAsString(userDTO))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getUsersTest() throws Exception {
        UserDTO userDTO = new UserDTO(UUID.randomUUID(), "john_doe", "John Doe", UserRole.CLIENT);
        when(service.findUsers()).thenReturn(Collections.singletonList(userDTO));

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john_doe"));
    }

    @Test
    public void getUserByIdTest() throws Exception {
        UUID id = UUID.randomUUID();
        UserDetailsDTO userDTO = new UserDetailsDTO(id, "john_doe", "secret123", "John Doe", UserRole.CLIENT);
        when(service.findUserById(id)).thenReturn(userDTO);

        mockMvc.perform(get("/user/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    public void deleteUserTest() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/user/" + id))
                .andExpect(status().isNoContent());
    }
}
