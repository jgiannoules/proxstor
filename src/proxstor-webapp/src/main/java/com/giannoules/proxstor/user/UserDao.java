/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.giannoules.proxstor.user;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author James_Giannoules
 */
public enum UserDao {
    instance;
    
    private Map<String, User> contentProvider = new HashMap<>();
    
    private UserDao() {}
    
    public Map<String, User> getModel() {
        return contentProvider;
    }
}
