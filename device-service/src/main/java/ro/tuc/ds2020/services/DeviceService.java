package ro.tuc.ds2020.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tuc.ds2020.controllers.handlers.exceptions.model.ResourceNotFoundException;
import ro.tuc.ds2020.dtos.DeviceDTO;
import ro.tuc.ds2020.dtos.DeviceDetailsDTO;
import ro.tuc.ds2020.dtos.builders.DeviceBuilder;
import ro.tuc.ds2020.entities.Device;
import ro.tuc.ds2020.repositories.DeviceRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceUserService deviceUserService;

    @Autowired
    public DeviceService(final DeviceRepository deviceRepository,
                         final DeviceUserService deviceUserService) {
        this.deviceRepository = deviceRepository;
        this.deviceUserService = deviceUserService;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO findDeviceById(final UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (deviceOptional.isEmpty()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDTO(deviceOptional.get());
    }

    public List<DeviceDTO> findDevicesByUserId(final UUID userId) {
        LOGGER.info("Retrieving devices for user ID: {}", userId);
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Device> devices = deviceRepository.findByUserId(userId);
        if (devices == null || devices.isEmpty()) {
            LOGGER.debug("Direct findByUserId query returned 0 devices for userId {}. Checking fallback...", userId);
            devices = deviceRepository.findAll().stream()
                    .filter(d -> userId.equals(d.getUserId()))
                    .collect(Collectors.toList());
        }

        LOGGER.info("Found {} devices for user ID {}", devices.size(), userId);
        return devices.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UUID insert(final DeviceDetailsDTO deviceDTO) {
        if (deviceDTO.getUserId() != null && !deviceUserService.userExists(deviceDTO.getUserId())) {
            LOGGER.error("Cannot assign device to non-existent user with id {}", deviceDTO.getUserId());
            throw new ResourceNotFoundException("User with id " + deviceDTO.getUserId() + " does not exist in Device Service");
        }

        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.info("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    @Transactional
    public DeviceDTO update(final UUID id, final DeviceDetailsDTO deviceDTO) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("Device with id {} was not found in db for update", id);
            return new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        });

        if (deviceDTO.getUserId() != null && !deviceUserService.userExists(deviceDTO.getUserId())) {
            LOGGER.error("Cannot reassign device to non-existent user with id {}", deviceDTO.getUserId());
            throw new ResourceNotFoundException("User with id " + deviceDTO.getUserId() + " does not exist in Device Service");
        }

        device.setName(deviceDTO.getName());
        device.setMaxConsumption(deviceDTO.getMaxConsumption());
        device.setDescription(deviceDTO.getDescription());
        device.setUserId(deviceDTO.getUserId());

        device = deviceRepository.save(device);
        LOGGER.info("Device with id {} was updated in db", device.getId());
        return DeviceBuilder.toDeviceDTO(device);
    }

    @Transactional
    public void delete(final UUID id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("Device with id {} was not found in db for deletion", id);
            return new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        });

        deviceRepository.delete(device);
        LOGGER.info("Device with id {} was deleted from db", id);
    }

    @Transactional
    public DeviceDTO assignDeviceToUser(final UUID deviceId, final UUID userId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() ->
                new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + deviceId));

        if (userId != null && !deviceUserService.userExists(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " does not exist in Device Service");
        }

        device.setUserId(userId);
        device = deviceRepository.save(device);
        LOGGER.info("Device {} successfully mapped to user {}", deviceId, userId);
        return DeviceBuilder.toDeviceDTO(device);
    }
}
