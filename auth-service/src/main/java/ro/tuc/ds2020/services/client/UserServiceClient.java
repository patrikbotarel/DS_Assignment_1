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
import ro.tuc.ds2020.dtos.RegisterRequestDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class UserServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8080/user}")
    private String userServiceUrl;

    @Value("${user.service.sync.enabled:true}")
    private boolean syncEnabled;

    @Autowired
    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void synchronizeUserCreation(UUID userId, RegisterRequestDTO requestDTO) {
        if (!syncEnabled) {
            LOGGER.info("User service synchronization is disabled by configuration.");
            return;
        }

        LOGGER.info("Initiating synchronization with User Microservice: POST {}", userServiceUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Call", "true");

            Map<String, Object> body = new HashMap<>();
            body.put("id", userId.toString());
            body.put("username", requestDTO.getUsername());
            body.put("password", requestDTO.getPassword());
            body.put("name", requestDTO.getName() != null ? requestDTO.getName() : requestDTO.getUsername());
            body.put("role", requestDTO.getRole() != null ? requestDTO.getRole().name() : "CLIENT");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    userServiceUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            LOGGER.info("User Microservice sync response: status={}", response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            LOGGER.warn("User Microservice returned HTTP {} during sync: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            LOGGER.warn("Could not connect to User Microservice at {}: {}", userServiceUrl, ex.getMessage());
        }
    }
}
