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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.ExceptionHandlerResponseDTO;
import ro.tuc.ds2020.dtos.UserDTO;
import ro.tuc.ds2020.dtos.UserDetailsDTO;
import ro.tuc.ds2020.dtos.UserUpdateDTO;
import ro.tuc.ds2020.services.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/user")
@Tag(name = "User Management", description = "Endpoints for managing user accounts and synchronizing with Device Microservice")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Retrieves a complete list of all users registered in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> dtos = userService.findUsers();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves detailed information of a single user by their unique UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found and returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found with specified UUID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<UserDetailsDTO> getUser(
            @Parameter(description = "UUID of the user to retrieve", required = true)
            @PathVariable("id") UUID userId) {
        UserDetailsDTO dto = userService.findUserById(userId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Create a new user",
            description = "Creates a new user (Admin or Client) and makes a synchronous HTTP call to Device Microservice to register the user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully; returns the generated UUID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for user fields",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Username already exists in the system",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<UUID> createUser(
            @Parameter(description = "Details of the user to be created", required = true)
            @Valid @RequestBody UserDetailsDTO userDTO) {
        UUID userId = userService.insert(userDTO);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing user", description = "Updates username, name, password, or role of an existing user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found with specified UUID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "New username already taken by another user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @PutMapping(value = "/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "UUID of the user to update", required = true)
            @PathVariable("id") UUID userId,
            @Parameter(description = "Updated user fields", required = true)
            @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.update(userId, userUpdateDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @Operation(summary = "Delete a user",
            description = "Deletes a user by UUID and performs a synchronous HTTP call to Device Microservice to remove user associations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found with specified UUID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionHandlerResponseDTO.class)))
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "UUID of the user to delete", required = true)
            @PathVariable("id") UUID userId) {
        userService.delete(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
