package ro.tuc.ds2020.services.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ro.tuc.ds2020.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://auth-service:8082/auth}")
    private String authServiceUrl;

    @Value("${auth.service.sync.enabled:true}")
    private boolean syncEnabled;

    @Autowired
    public AuthServiceClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String getEndpointUrl(final String relativePath) {
        String base = authServiceUrl != null ? authServiceUrl.trim() : "http://auth-service:8082/auth";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!base.endsWith("/auth")) {
            base = base + "/auth";
        }
        return base + relativePath;
    }

    public void synchronizeUserCreation(final User user) {
        if (!syncEnabled) {
            LOGGER.info("Auth service synchronization is disabled by configuration.");
            return;
        }

        String targetUrl = getEndpointUrl("/sync/user");
        LOGGER.info("Initiating credentials sync to Auth Service: POST {}", targetUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Call", "true");

            Map<String, Object> body = new HashMap<>();
            body.put("id", user.getId().toString());
            body.put("username", user.getUsername());
            body.put("password", user.getPassword());
            body.put("role", user.getRole() != null ? user.getRole().name() : "CLIENT");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, Void.class);
            LOGGER.info("Successfully synced credentials for user '{}' to Auth Service", user.getUsername());
        } catch (HttpStatusCodeException ex) {
            LOGGER.error("Auth Service returned HTTP {} during user credentials sync: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Auth Service rejected credentials synchronization (HTTP "
                    + ex.getStatusCode().value() + "): " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            LOGGER.error("Could not reach Auth Service at {}: {}", targetUrl, ex.getMessage());
            throw new RuntimeException("Could not connect to Auth Service at " + targetUrl + ": " + ex.getMessage(), ex);
        }
    }

    public void synchronizeUserDeletion(final UUID userId) {
        if (!syncEnabled) {
            return;
        }

        String targetUrl = getEndpointUrl("/sync/user/" + userId);
        LOGGER.info("Initiating deletion sync to Auth Service: DELETE {}", targetUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Call", "true");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            restTemplate.exchange(targetUrl, HttpMethod.DELETE, requestEntity, Void.class);
            LOGGER.info("Successfully deleted user credentials with ID {} in Auth Service", userId);
        } catch (HttpStatusCodeException ex) {
            LOGGER.error("Auth Service returned HTTP {} during deletion sync: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            LOGGER.error("Could not reach Auth Service at {}: {}", targetUrl, ex.getMessage());
        }
    }
}
