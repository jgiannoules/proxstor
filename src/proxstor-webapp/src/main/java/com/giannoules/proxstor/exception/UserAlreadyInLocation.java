package com.giannoules.proxstor.exception;

public class UserAlreadyInLocation extends Exception {
 
    public UserAlreadyInLocation(String s) {
        super(s);
    }
    
    public UserAlreadyInLocation() {
        super();
    }
}
