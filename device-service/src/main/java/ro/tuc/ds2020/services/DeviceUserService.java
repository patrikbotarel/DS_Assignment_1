package ro.tuc.ds2020.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tuc.ds2020.entities.Device;
import ro.tuc.ds2020.entities.DeviceUser;
import ro.tuc.ds2020.repositories.DeviceRepository;
import ro.tuc.ds2020.repositories.DeviceUserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceUserService.class);

    private final DeviceUserRepository deviceUserRepository;
    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceUserService(final DeviceUserRepository deviceUserRepository,
                             final DeviceRepository deviceRepository) {
        this.deviceUserRepository = deviceUserRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * Registers a new user ID received from User Management Microservice.
     */
    @Transactional
    public void registerUser(final UUID userId) {
        if (!deviceUserRepository.existsById(userId)) {
            DeviceUser user = new DeviceUser(userId);
            deviceUserRepository.save(user);
            LOGGER.info("User with id {} synchronized and registered in Device Service.", userId);
        } else {
            LOGGER.info("User with id {} already exists in Device Service local storage.", userId);
        }
    }

    /**
     * Removes user ID and unlinks/cascades all devices associated with the deleted user.
     */
    @Transactional
    public void deleteUser(final UUID userId) {
        // Disassociate devices linked to this user to preserve device records,
        // or clear user assignment while maintaining consistency
        List<Device> userDevices = deviceRepository.findByUserId(userId);
        for (Device device : userDevices) {
            device.setUserId(null);
            deviceRepository.save(device);
            LOGGER.info("Unlinked device {} from deleted user {}.", device.getId(), userId);
        }

        if (deviceUserRepository.existsById(userId)) {
            deviceUserRepository.deleteById(userId);
            LOGGER.info("User with id {} removed from Device Service local storage.", userId);
        } else {
            LOGGER.warn("User with id {} was not found in Device Service local storage.", userId);
        }
    }

    public boolean userExists(final UUID userId) {
        return deviceUserRepository.existsById(userId);
    }

    public List<UUID> findAllUserIds() {
        return deviceUserRepository.findAll().stream()
                .map(DeviceUser::getId)
                .collect(Collectors.toList());
    }
}
