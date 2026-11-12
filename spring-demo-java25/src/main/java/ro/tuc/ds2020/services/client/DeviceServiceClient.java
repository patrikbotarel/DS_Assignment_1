package ro.tuc.ds2020.services.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class DeviceServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${device.service.url:http://localhost:8081/device}")
    private String deviceServiceUrl;

    @Value("${device.service.sync.enabled:true}")
    private boolean syncEnabled;

    @Autowired
    public DeviceServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Synchronously notifies Device Microservice of a user creation.
     * Target endpoint: POST {device.service.url}/user/{userId}
     *
     * @param userId UUID of the newly created user
     */
    public void synchronizeUserCreation(UUID userId) {
        if (!syncEnabled) {
            LOGGER.info("Device synchronization is disabled by configuration.");
            return;
        }

        String targetUrl = deviceServiceUrl + "/user/" + userId;
        LOGGER.info("Initiating synchronous HTTP call to Device Microservice to register user: POST {}", targetUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            LOGGER.info("Device Microservice successfully synchronized user {}. Status: {}", userId, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            LOGGER.error("Device Microservice responded with error status {} while registering user {}: {}",
                    ex.getStatusCode(), userId, ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            LOGGER.warn("Device Microservice is unreachable at {} (Reason: {}). "
                            + "User {} created in User DB; will be synchronized once Device Microservice is running.",
                    targetUrl, ex.getMessage(), userId);
        }
    }

    /**
     * Synchronously notifies Device Microservice of a user deletion.
     * Target endpoint: DELETE {device.service.url}/user/{userId}
     *
     * @param userId UUID of the user being deleted
     */
    public void synchronizeUserDeletion(UUID userId) {
        if (!syncEnabled) {
            LOGGER.info("Device synchronization is disabled by configuration.");
            return;
        }

        String targetUrl = deviceServiceUrl + "/user/" + userId;
        LOGGER.info("Initiating synchronous HTTP call to Device Microservice to remove user: DELETE {}", targetUrl);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            LOGGER.info("Device Microservice successfully removed associations for user {}. Status: {}", userId, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            LOGGER.error("Device Microservice responded with error status {} while deleting user {}: {}",
                    ex.getStatusCode(), userId, ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            LOGGER.warn("Device Microservice is unreachable at {} (Reason: {}). "
                            + "User {} deleted from User DB; will be synchronized once Device Microservice is running.",
                    targetUrl, ex.getMessage(), userId);
        }
    }
}
