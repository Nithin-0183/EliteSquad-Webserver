package com.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WebServerController {

    @GetMapping("/resource")
    public ResponseEntity<String> getResource() {
        return new ResponseEntity<>("Resource fetched successfully", HttpStatus.OK);
    }

    @PostMapping("/resource")
    public ResponseEntity<String> createResource(@RequestBody String resource) {
        if (resource == null || resource.isEmpty()) {
            return new ResponseEntity<>("Invalid resource", HttpStatus.BAD_REQUEST);
        }
        // Code to create resource
        return new ResponseEntity<>("Resource created successfully", HttpStatus.CREATED);
    }

    @PutMapping("/resource")
    public ResponseEntity<String> updateResource(@RequestBody String resource) {
        if (resource == null || resource.isEmpty()) {
            return new ResponseEntity<>("Invalid resource", HttpStatus.BAD_REQUEST);
        }
        // Code to update resource
        return new ResponseEntity<>("Resource updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<String> deleteResource() {
        // Code to delete resource
        return new ResponseEntity<>("Resource deleted successfully", HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
