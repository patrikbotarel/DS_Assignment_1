package ro.tuc.ds2020.dtos.builders;

import ro.tuc.ds2020.dtos.DeviceDTO;
import ro.tuc.ds2020.dtos.DeviceDetailsDTO;
import ro.tuc.ds2020.entities.Device;

public final class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getDescription(),
                device.getUserId()
        );
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getDescription(),
                device.getUserId()
        );
    }

    public static Device toEntity(DeviceDetailsDTO dto) {
        return new Device(
                dto.getName(),
                dto.getMaxConsumption(),
                dto.getDescription(),
                dto.getUserId()
        );
    }
}
