package com.example;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DemoService {

    private final Map<String, String> dataStore = new ConcurrentHashMap<>();

    public String getSite(String site) {
        return dataStore.get(site);
    }

    public boolean createSite(String site, String content) {
        if (dataStore.containsKey(site)) {
            return false;
        } else {
            dataStore.put(site, content);
            return true;
        }
    }

    public boolean updateSite(String site, String content) {
        if (dataStore.containsKey(site)) {
            dataStore.put(site, content);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteSite(String site) {
        if (dataStore.containsKey(site)) {
            dataStore.remove(site);
            return true;
        } else {
            return false;
        }
    }
}
