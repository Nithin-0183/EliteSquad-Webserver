package com.ues.adminportal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ues.adminportal.entity.Site;
import com.ues.adminportal.entity.Status;
import com.ues.adminportal.entity.User;
import com.ues.adminportal.repository.SiteRepository;
import com.ues.adminportal.repository.StatusRepository;
import com.ues.adminportal.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

     private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusRepository statusRepository;

    @GetMapping("/test")
    public String testEndpoint() {
        return "Controller is working!";
    }
    

    @GetMapping("/server-status")
    public ResponseEntity<List<Site>> getServerStatus() {
        logger.error("API HITTTTT::::::");
        List<Site> sites = siteRepository.findAll();
        logger.info("sites list:: "+sites.toString());
        return ResponseEntity.ok(sites);
    }


    @PostMapping("/add-server")
    public ResponseEntity<Map<String, String>> addServer(@RequestParam("domain") String domain,
                                            @RequestParam("ipAddress") String ipAddress,
                                            @RequestParam("userId") int userId,
                                            @RequestParam("zipFile") MultipartFile zipFile) {
        try {
            // Define the upload path
            String uploadDir = "/WEB_ROOT/" + domain;
            //File targetFile = new File(uploadDir);
            
            // Create directories if they do not exist
            // if (!targetFile.exists()) {
            //     targetFile.mkdirs();
            // }
            
            // Save the uploaded file
            //File uploadDestination = new File(uploadDir, zipFile.getOriginalFilename());
            //zipFile.transferTo(uploadDestination);
            String uploadDestination = "Upload/"+domain;

            // Find the running status
            Status runningStatus = statusRepository.findById(12100)
            .orElseThrow(() -> new IllegalArgumentException("Invalid status ID"));
            User user = userRepository.findById((long) userId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

            // Create a new Site entity and save it to the repository
            Site site = new Site();
            site.setDomain(domain);
            site.setRoot(uploadDir);
            site.setIpAddress(ipAddress);
            site.setUser(user);
            site.setStatus(runningStatus);
            site.setUploadPath(uploadDestination);
            site.setCreatedAt(LocalDateTime.now()); 
            site.setTimestamp(LocalDateTime.now()); 
            siteRepository.save(site);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Website added successfully");
            response.put("domain", domain);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error and return a server error response
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "An error occurred: " + e.getMessage()));
        }
    }


    @PostMapping("/remove-server")
    public ResponseEntity<String> removeServer(@RequestParam Long siteId) {
        siteRepository.deleteById(siteId);
        return ResponseEntity.ok("Website removed successfully");
    }
}