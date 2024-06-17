package com.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/site")
public class DemoController {

    private Map<String, String> dataStore = new ConcurrentHashMap<>();

    @GetMapping("/{site}")
    public ResponseEntity<String> getSite(@PathVariable String site) {
        String content = dataStore.get(site);
        if (content != null) {
            return new ResponseEntity<>(content, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Site not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{site}")
    public ResponseEntity<String> createSite(@PathVariable String site, @RequestBody String content) {
        if (dataStore.containsKey(site)) {
            return new ResponseEntity<>("Site already exists", HttpStatus.CONFLICT);
        } else {
            dataStore.put(site, content);
            return new ResponseEntity<>("Site created", HttpStatus.CREATED);
        }
    }

    @PutMapping("/{site}")
    public ResponseEntity<String> updateSite(@PathVariable String site, @RequestBody String content) {
        if (dataStore.containsKey(site)) {
            dataStore.put(site, content);
            return new ResponseEntity<>("Site updated", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Site not found", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{site}")
    public ResponseEntity<String> deleteSite(@PathVariable String site) {
        if (dataStore.containsKey(site)) {
            dataStore.remove(site);
            return new ResponseEntity<>("Site deleted", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Site not found", HttpStatus.NOT_FOUND);
        }
    }
}
