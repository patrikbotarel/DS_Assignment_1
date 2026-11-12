package ro.tuc.ds2020.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.tuc.ds2020.entities.UserCredential;
import ro.tuc.ds2020.entities.UserRole;
import ro.tuc.ds2020.repositories.UserCredentialRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserCredentialRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!repository.existsByUsername("admin")) {
            UserCredential admin = new UserCredential(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    UserRole.ADMIN
            );
            repository.save(admin);
            LOGGER.info("Seeded default administrator credentials: admin / admin123 (ADMIN)");
        }

        if (!repository.existsByUsername("client")) {
            UserCredential client = new UserCredential(
                    "client",
                    passwordEncoder.encode("client123"),
                    UserRole.CLIENT
            );
            repository.save(client);
            LOGGER.info("Seeded default client credentials: client / client123 (CLIENT)");
        }
    }
}
