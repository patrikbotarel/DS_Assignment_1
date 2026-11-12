package ro.tuc.ds2020.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.DuplicateResourceException;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.ResourceNotFoundException;
import ro.tuc.ds2020.dtos.UserDTO;
import ro.tuc.ds2020.dtos.UserDetailsDTO;
import ro.tuc.ds2020.dtos.UserUpdateDTO;
import ro.tuc.ds2020.dtos.builders.UserBuilder;
import ro.tuc.ds2020.entities.User;
import ro.tuc.ds2020.repositories.UserRepository;
import ro.tuc.ds2020.services.client.AuthServiceClient;
import ro.tuc.ds2020.services.client.DeviceServiceClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final DeviceServiceClient deviceServiceClient;
    private final AuthServiceClient authServiceClient;

    @Autowired
    public UserService(final UserRepository userRepository,
                       final DeviceServiceClient deviceServiceClient,
                       final AuthServiceClient authServiceClient) {
        this.userRepository = userRepository;
        this.deviceServiceClient = deviceServiceClient;
        this.authServiceClient = authServiceClient;
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(final UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        return UserBuilder.toUserDetailsDTO(userOptional.get());
    }

    @Transactional(rollbackFor = Exception.class)
    public UUID insert(final UserDetailsDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            LOGGER.error("Username {} already exists in database", userDTO.getUsername());
            throw new DuplicateResourceException("Username '" + userDTO.getUsername() + "' already taken");
        }

        User user = UserBuilder.toEntity(userDTO);
        user = userRepository.save(user);
        LOGGER.info("User with id {} was inserted in db", user.getId());

        try {
            // Synchronous HTTP call to Device Microservice to register user ID
            deviceServiceClient.synchronizeUserCreation(user.getId());

            // Synchronous HTTP call to Auth Microservice to register credentials
            authServiceClient.synchronizeUserCreation(user);
        } catch (Exception ex) {
            LOGGER.error("Inter-service synchronization failed for user '{}'. Rolling back local database insert.",
                    user.getUsername(), ex);

            // Compensate device service if user was already synced there
            try {
                deviceServiceClient.synchronizeUserDeletion(user.getId());
            } catch (Exception compEx) {
                LOGGER.warn("Failed to compensate device service for user {}: {}", user.getId(), compEx.getMessage());
            }

            // Throw exception to trigger Spring @Transactional rollback on energy_db
            throw new RuntimeException("User registration failed due to authentication sync error: " + ex.getMessage(), ex);
        }

        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public UserDTO update(final UUID id, final UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("User with id {} was not found in db for update", id);
            return new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        });

        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateDTO.getUsername())) {
                LOGGER.error("Cannot update: username {} is already taken", updateDTO.getUsername());
                throw new DuplicateResourceException("Username '" + updateDTO.getUsername() + "' already taken");
            }
            user.setUsername(updateDTO.getUsername());
        }

        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isBlank()) {
            user.setPassword(updateDTO.getPassword());
        }

        if (updateDTO.getRole() != null) {
            user.setRole(updateDTO.getRole());
        }

        user = userRepository.save(user);
        LOGGER.info("User with id {} was updated in db", user.getId());

        // Synchronize updated credentials with Auth Microservice
        authServiceClient.synchronizeUserCreation(user);

        return UserBuilder.toUserDTO(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(final UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("User with id {} was not found in db for deletion", id);
            return new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        });

        userRepository.delete(user);
        LOGGER.info("User with id {} was deleted from db", id);

        // Synchronous HTTP call to Device Microservice to delete user associations
        deviceServiceClient.synchronizeUserDeletion(id);

        // Synchronous HTTP call to Auth Microservice to delete credentials
        authServiceClient.synchronizeUserDeletion(id);
    }
}
