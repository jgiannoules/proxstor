package com.giannoules.proxstor.exception;

public class UserCurrentlyInMultipleLocalities extends Exception {
        
    public UserCurrentlyInMultipleLocalities(String s) {
        super(s);
    }
    
    public UserCurrentlyInMultipleLocalities() {
        super();
    }
    
}
