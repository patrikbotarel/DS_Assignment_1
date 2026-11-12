package ro.tuc.ds2020;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication(excludeName = {
        "org.springdoc.core.configuration.SpringDocHateoasConfiguration",
        "org.springdoc.core.configuration.SpringDocDataRestConfiguration"
})
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class DeviceServiceApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}
