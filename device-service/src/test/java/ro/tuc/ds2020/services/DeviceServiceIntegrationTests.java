package ro.tuc.ds2020.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import ro.tuc.ds2020.DeviceServiceTestConfig;
import ro.tuc.ds2020.dtos.DeviceDTO;
import ro.tuc.ds2020.dtos.DeviceDetailsDTO;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/test-sql/create.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:/test-sql/delete.sql")
public class DeviceServiceIntegrationTests extends DeviceServiceTestConfig {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceUserService deviceUserService;

    @Test
    public void testGetDevices() {
        List<DeviceDTO> devices = deviceService.findDevices();
        assertEquals(1, devices.size(), "Pre-populated devices count");
        assertEquals("Smart Meter Living", devices.get(0).getName());
    }

    @Test
    public void testInsertDeviceWithSyncedUser() {
        UUID syncedUserId = UUID.randomUUID();
        deviceUserService.registerUser(syncedUserId);

        DeviceDetailsDTO dto = new DeviceDetailsDTO("Smart Meter Bedroom", 180.0, "Bedroom AC", syncedUserId);
        UUID deviceId = deviceService.insert(dto);

        DeviceDTO fetched = deviceService.findDeviceById(deviceId);
        assertNotNull(fetched);
        assertEquals("Smart Meter Bedroom", fetched.getName());
        assertEquals(syncedUserId, fetched.getUserId());
    }

    @Test
    public void testDeleteSyncedUserDisassociatesDevice() {
        UUID prePopulatedUserId = UUID.fromString("00201d1e-61f3-4015-88bf-4292b86e22e4");

        List<DeviceDTO> before = deviceService.findDevicesByUserId(prePopulatedUserId);
        assertEquals(1, before.size());

        deviceUserService.deleteUser(prePopulatedUserId);

        List<DeviceDTO> after = deviceService.findDevicesByUserId(prePopulatedUserId);
        assertEquals(0, after.size(), "User should have no associated devices after deletion");

        List<DeviceDTO> allDevices = deviceService.findDevices();
        assertEquals(1, allDevices.size(), "Device record should still exist, but unlinked");
        assertNull(allDevices.get(0).getUserId(), "Device userId should be null");
    }
}
