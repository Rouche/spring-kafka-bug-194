package ms.kafkatest.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MockResource {

    /**
     * GET example
     *
     * @param option
     * @return JSON
     */
    @GetMapping(value = "/_example/{option}.json", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity testGet(@PathVariable String option) {
        switch (option) {
            case "9999":
                return ResponseEntity.ok("{\"option\":\"9999\", \"client\":\"12345\"}");
            case "401":
                return ResponseEntity.status(401).build();
            case "403":
                return ResponseEntity.status(403).build();
            case "404":
                return ResponseEntity.status(404).build();
            case "500":
                return ResponseEntity.status(500).build();
            default:
                return ResponseEntity.badRequest().build();
        }
    }
}
