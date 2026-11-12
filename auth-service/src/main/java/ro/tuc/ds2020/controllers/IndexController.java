package ro.tuc.ds2020.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Tag(name = "Health Check", description = "Microservice health check endpoint")
public class IndexController {

    @Operation(summary = "Health check / service status endpoint", description = "Returns running status of Authorization microservice")
    @GetMapping(value = "/")
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("Authorization Service is running.", HttpStatus.OK);
    }
}
