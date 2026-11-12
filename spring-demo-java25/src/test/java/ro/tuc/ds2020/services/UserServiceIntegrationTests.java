package ro.tuc.ds2020.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import ro.tuc.ds2020.Ds2020TestConfig;
import ro.tuc.ds2020.dtos.UserDTO;
import ro.tuc.ds2020.dtos.UserDetailsDTO;
import ro.tuc.ds2020.dtos.UserUpdateDTO;
import ro.tuc.ds2020.entities.UserRole;
import ro.tuc.ds2020.services.client.AuthServiceClient;
import ro.tuc.ds2020.services.client.DeviceServiceClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/test-sql/create.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:/test-sql/delete.sql")
public class UserServiceIntegrationTests extends Ds2020TestConfig {

    @Autowired
    private UserService userService;

    @MockitoBean
    private DeviceServiceClient deviceServiceClient;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @Test
    public void testGetCorrect() {
        List<UserDTO> userDTOList = userService.findUsers();
        assertEquals(1, userDTOList.size(), "Pre-populated user count");
        assertEquals("admin_test", userDTOList.get(0).getUsername());
    }

    @Test
    public void testInsertCorrectWithGetById() {
        UserDetailsDTO u = new UserDetailsDTO("client_user", "client_pass", "Client User", UserRole.CLIENT);
        UUID insertedId = userService.insert(u);

        UserDetailsDTO fetched = userService.findUserById(insertedId);
        assertNotNull(fetched);
        assertEquals("client_user", fetched.getUsername());
        assertEquals(UserRole.CLIENT, fetched.getRole());
    }

    @Test
    public void testInsertCorrectWithGetAll() {
        UserDetailsDTO u = new UserDetailsDTO("client_user2", "client_pass2", "Client User 2", UserRole.CLIENT);
        userService.insert(u);

        List<UserDTO> userDTOList = userService.findUsers();
        assertEquals(2, userDTOList.size(), "Total users after insert");
    }

    @Test
    public void testUpdateUser() {
        UserDetailsDTO u = new UserDetailsDTO("update_user", "pass", "Name", UserRole.CLIENT);
        UUID id = userService.insert(u);

        UserUpdateDTO updateDTO = new UserUpdateDTO("updated_user", "new_pass", "New Name", UserRole.ADMIN);
        UserDTO result = userService.update(id, updateDTO);

        assertEquals("updated_user", result.getUsername());
        assertEquals("New Name", result.getName());
        assertEquals(UserRole.ADMIN, result.getRole());
    }

    @Test
    public void testDeleteUser() {
        UserDetailsDTO u = new UserDetailsDTO("delete_user", "pass", "Name", UserRole.CLIENT);
        UUID id = userService.insert(u);

        userService.delete(id);
        List<UserDTO> list = userService.findUsers();
        assertEquals(1, list.size(), "Should remain only initial pre-populated user");
    }
}
