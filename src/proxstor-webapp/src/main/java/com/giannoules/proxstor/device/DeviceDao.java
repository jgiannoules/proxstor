package com.giannoules.proxstor.device;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum DeviceDao {
    instance;
    
    private Map<String, Map<String, Device>> contentProvider = new HashMap<>();
    
    private DeviceDao() {}
    
    private Map<String, Map<String, Device>> getModel() {
        return contentProvider;
    }
    
    public Device getDevice(String userId, String devId) {
        if (instance.contentProvider.containsKey(userId)) {
            return instance.contentProvider.get(userId).get(devId);        
        }
        return null;
    }
    
    public Collection<Device> getAllDevices(String userId) {
        if (instance.contentProvider.containsKey(userId)) {
            return instance.contentProvider.get(userId).values();
        }
        return Collections.EMPTY_SET;
    }
    
    public Device addDevice(String userId, Device dev) {
        if (!instance.contentProvider.containsKey(userId)) {
            Map<String, Device> devices = new HashMap<>();
            instance.contentProvider.put(userId, devices);
        }
        Device newDev = new Device();
        newDev.setDescription(dev.getDescription());
        newDev.setDevId(UUID.randomUUID().toString());            
        instance.contentProvider.get(userId).put(newDev.getDevId(), newDev);
        return newDev;
    }
    
    public boolean updateDevice(String userId, Device dev) {
        if (instance.validDeviceId(userId, dev.getDevId())) {
            instance.contentProvider.get(userId).put(dev.getDevId(), dev);
            return true;
        }
        return false;
    }
    
    public boolean deleteDevice(String userId, String devId) {
        if (instance.validDeviceId(userId, devId)) {
            instance.contentProvider.remove(userId);
            return true;
        }
        return false;
    }
    
    public boolean validDeviceId(String userId, String devId) {
        return (instance.contentProvider.containsKey(userId) &&
                instance.contentProvider.get(userId).containsKey(devId));
    }
}
