package ro.tuc.ds2020.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.ExceptionHandlerResponseDTO;
import ro.tuc.ds2020.dtos.DeviceDTO;
import ro.tuc.ds2020.dtos.DeviceDetailsDTO;
import ro.tuc.ds2020.services.DeviceService;
import ro.tuc.ds2020.services.DeviceUserService;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/device")
@Tag(name = "Device Management", description = "Endpoints for managing energy metering devices and user associations")
public class DeviceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceController.class);

    private final DeviceService deviceService;
    private final DeviceUserService deviceUserService;

    @Autowired
    public DeviceController(final DeviceService deviceService,
                            final DeviceUserService deviceUserService) {
        this.deviceService = deviceService;
        this.deviceUserService = deviceUserService;
    }

    @Operation(summary = "Get all devices", description = "Retrieves a complete list of all smart energy metering devices.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devices successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        List<DeviceDTO> dtos = deviceService.findDevices();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Get device by ID", description = "Retrieves information of a specific device by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found and returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Device not found with specified UUID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<DeviceDTO> getDevice(
            @Parameter(description = "UUID of the device to retrieve", required = true)
            @PathVariable("id") final UUID deviceId) {
        DeviceDTO dto = deviceService.findDeviceById(deviceId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Get devices by User ID", description = "Retrieves all devices assigned to a specific user (used by Client view).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of devices linked to the user",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class))))
    })
    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUserId(
            @Parameter(description = "UUID of the user", required = true)
            @PathVariable("userId") final UUID userId) {
        LOGGER.info("Fetching devices assigned to user ID: {}", userId);
        List<DeviceDTO> dtos = deviceService.findDevicesByUserId(userId);
        LOGGER.info("Returning {} devices for user ID: {}", dtos.size(), userId);
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Create a new device", description = "Registers a new energy metering device into the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for device attributes",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Referenced user does not exist",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<UUID> createDevice(
            @Parameter(description = "Details of the device to create", required = true)
            @Valid @RequestBody final DeviceDetailsDTO deviceDTO) {
        UUID deviceId = deviceService.insert(deviceDTO);
        return new ResponseEntity<>(deviceId, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing device", description = "Modifies device properties such as name, max consumption, or assigned user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Device or assigned user not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @PutMapping(value = "/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @Parameter(description = "UUID of the device to update", required = true)
            @PathVariable("id") final UUID deviceId,
            @Parameter(description = "Updated device payload", required = true)
            @Valid @RequestBody final DeviceDetailsDTO deviceDTO) {
        DeviceDTO updatedDevice = deviceService.update(deviceId, deviceDTO);
        return new ResponseEntity<>(updatedDevice, HttpStatus.OK);
    }

    @Operation(summary = "Delete a device", description = "Deletes an energy metering device by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found with specified UUID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "UUID of the device to delete", required = true)
            @PathVariable("id") final UUID deviceId) {
        deviceService.delete(deviceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Assign device to user", description = "Maps an existing device to a user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device successfully assigned to user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Device or user not found")
    })
    @RequestMapping(value = "/{id}/assign/{userId}", method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<DeviceDTO> assignDevice(
            @Parameter(description = "UUID of the device", required = true)
            @PathVariable("id") final UUID deviceId,
            @Parameter(description = "UUID of the user to assign", required = true)
            @PathVariable("userId") final UUID userId) {
        DeviceDTO updated = deviceService.assignDeviceToUser(deviceId, userId);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @Operation(summary = "Unassign device from user", description = "Removes user mapping from a device.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device successfully unassigned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @RequestMapping(value = "/{id}/unassign", method = {
            RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<DeviceDTO> unassignDevice(
            @Parameter(description = "UUID of the device", required = true)
            @PathVariable("id") final UUID deviceId) {
        DeviceDTO updated = deviceService.assignDeviceToUser(deviceId, null);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Synchronous Inter-Service Communication Endpoints (called by User Service)
    // -------------------------------------------------------------------------

    @Operation(summary = "Synchronize user creation (Internal)",
            description = "Internal endpoint called synchronously by User Microservice when a new user is created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User ID successfully registered in Device Service local storage")
    })
    @PostMapping(value = "/user/{userId}")
    public ResponseEntity<Void> syncUserCreation(
            @Parameter(description = "UUID of the user created in User Service", required = true)
            @PathVariable("userId") final UUID userId) {
        deviceUserService.registerUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Synchronize user deletion (Internal)",
            description = "Internal endpoint called synchronously by User Microservice when a user is deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User ID removed and associated devices disassociated")
    })
    @DeleteMapping(value = "/user/{userId}")
    public ResponseEntity<Void> syncUserDeletion(
            @Parameter(description = "UUID of the user deleted in User Service", required = true)
            @PathVariable("userId") final UUID userId) {
        deviceUserService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "List all synced users (Internal/Debug)",
            description = "Retrieves all user IDs currently known to Device Microservice.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User IDs successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UUID.class))))
    })
    @GetMapping(value = "/user")
    public ResponseEntity<List<UUID>> getSyncedUsers() {
        List<UUID> users = deviceUserService.findAllUserIds();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
