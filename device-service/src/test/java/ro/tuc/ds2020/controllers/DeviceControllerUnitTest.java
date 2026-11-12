package ro.tuc.ds2020.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.tuc.ds2020.DeviceServiceTestConfig;
import ro.tuc.ds2020.dtos.DeviceDTO;
import ro.tuc.ds2020.dtos.DeviceDetailsDTO;
import ro.tuc.ds2020.security.SecurityInterceptor;
import ro.tuc.ds2020.services.DeviceService;
import ro.tuc.ds2020.services.DeviceUserService;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeviceControllerUnitTest extends DeviceServiceTestConfig {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @MockitoBean
    private DeviceUserService deviceUserService;

    @MockitoBean
    private SecurityInterceptor securityInterceptor;

    @BeforeEach
    public void setup() throws Exception {
        when(securityInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    public void createDeviceTest() throws Exception {
        DeviceDetailsDTO dto = new DeviceDetailsDTO("Smart Meter A", 120.0, "Kitchen meter", UUID.randomUUID());
        when(deviceService.insert(any(DeviceDetailsDTO.class))).thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/device")
                .content(jsonMapper.writeValueAsString(dto))
                .contentType("application/json"))
                .andExpect(status().isCreated());
    }

    @Test
    public void createDeviceFailsDueToBlankName() throws Exception {
        DeviceDetailsDTO dto = new DeviceDetailsDTO("", 120.0, "Kitchen meter", UUID.randomUUID());

        mockMvc.perform(post("/device")
                .content(jsonMapper.writeValueAsString(dto))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDevicesTest() throws Exception {
        DeviceDTO dto = new DeviceDTO(UUID.randomUUID(), "Smart Meter B", 200.0, "Living room", null);
        when(deviceService.findDevices()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Smart Meter B"));
    }

    @Test
    public void getDevicesByUserIdTest() throws Exception {
        UUID userId = UUID.randomUUID();
        DeviceDTO dto = new DeviceDTO(UUID.randomUUID(), "Smart Meter C", 150.0, "Garage", userId);
        when(deviceService.findDevicesByUserId(userId)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/device/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Smart Meter C"))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    public void syncUserCreationTest() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/device/user/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    public void syncUserDeletionTest() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/device/user/" + userId))
                .andExpect(status().isNoContent());
    }
}
