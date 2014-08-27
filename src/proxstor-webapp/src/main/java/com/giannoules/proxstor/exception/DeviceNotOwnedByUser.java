package com.giannoules.proxstor.exception;

public class DeviceNotOwnedByUser extends Exception {
    
    public DeviceNotOwnedByUser(String s) {
        super(s);
    }
    
    public DeviceNotOwnedByUser() {
        super();
    }
    
}
