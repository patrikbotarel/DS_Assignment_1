package ro.tuc.ds2020.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "Device name is mandatory")
    private String name;

    @NotNull(message = "Maximum consumption is mandatory")
    @Positive(message = "Maximum consumption must be positive")
    private Double maxConsumption;

    private String description;

    private UUID userId;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(String name, Double maxConsumption, String description, UUID userId) {
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.description = description;
        this.userId = userId;
    }

    public DeviceDetailsDTO(UUID id, String name, Double maxConsumption, String description, UUID userId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.description = description;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(Double maxConsumption) {
        this.maxConsumption = maxConsumption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceDetailsDTO that = (DeviceDetailsDTO) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(maxConsumption, that.maxConsumption)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, maxConsumption, userId);
    }
}
