package ro.tuc.ds2020.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String JWT_HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private static final Pattern SUB_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern EXP_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*([0-9]+)");

    @Value("${jwt.secret:mySecretJwtSigningKeyEnergyManagementSystem2026DistributedSystemsAssignment}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private long expirationInSeconds;

    public JwtUtil() {
    }

    public JwtUtil(String secret, long expirationInSeconds) {
        this.secret = secret;
        this.expirationInSeconds = expirationInSeconds;
    }

    public String generateToken(UUID userId, String username, String role) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expirationInSeconds;

        String headerEncoded = base64UrlEncode(JWT_HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"userId\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
                escapeJson(username),
                userId != null ? userId.toString() : "",
                escapeJson(role),
                now,
                exp
        );
        String payloadEncoded = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signatureData = headerEncoded + "." + payloadEncoded;
        byte[] signatureBytes = computeHmacSha256(signatureData.getBytes(StandardCharsets.UTF_8), secret);
        String signatureEncoded = base64UrlEncode(signatureBytes);

        return signatureData + "." + signatureEncoded;
    }

    public JwtClaims validateToken(String token) {
        if (token == null || token.isBlank()) {
            LOGGER.warn("Token is null or blank");
            return null;
        }

        String cleanToken = token.trim();
        if (cleanToken.startsWith("Bearer ") || cleanToken.startsWith("bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
        }

        String[] parts = cleanToken.split("\\.");
        if (parts.length != 3) {
            LOGGER.warn("Token does not have 3 parts");
            return null;
        }

        String dataToVerify = parts[0] + "." + parts[1];
        byte[] expectedSignature = computeHmacSha256(dataToVerify.getBytes(StandardCharsets.UTF_8), secret);
        byte[] actualSignature;
        try {
            actualSignature = base64UrlDecode(parts[2]);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Signature is not valid Base64URL: {}", e.getMessage());
            return null;
        }

        if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
            LOGGER.warn("JWT signature verification failed");
            return null;
        }

        String payloadJson;
        try {
            payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Payload is not valid Base64URL: {}", e.getMessage());
            return null;
        }

        Long exp = extractLong(EXP_PATTERN, payloadJson);
        if (exp == null) {
            LOGGER.warn("JWT missing exp claim");
            return null;
        }

        long now = Instant.now().getEpochSecond();
        if (now > exp) {
            LOGGER.warn("JWT token is expired: now={}, exp={}", now, exp);
            return null;
        }

        String sub = extractString(SUB_PATTERN, payloadJson);
        String userIdStr = extractString(USER_ID_PATTERN, payloadJson);
        String role = extractString(ROLE_PATTERN, payloadJson);

        UUID userId = null;
        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid UUID in token: {}", userIdStr);
            }
        }

        return new JwtClaims(sub, userId, role, exp);
    }

    public long getExpirationInSeconds() {
        return expirationInSeconds;
    }

    private byte[] computeHmacSha256(byte[] data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256", e);
        }
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] base64UrlDecode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }

    private String escapeJson(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractString(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Long extractLong(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
