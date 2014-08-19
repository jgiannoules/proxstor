/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giannoules.proxstor.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author James_Giannoules
 */
public enum UserDao {
    instance;

    private Map<String, User> contentProvider = new HashMap<>();

    private UserDao() {
    }

    private Map<String, User> getModel() {
        return contentProvider;
    }

    public User getUser(String userId) {
         if (instance.contentProvider.containsKey(userId)) {
            return instance.contentProvider.get(userId);
         }
        return null;
    }

    public Collection<User> getAllUsers() {
        return instance.contentProvider.values();
    }

    public User addUser(User u) {
        if (u.getUserId() == null) {
            User newUser = new User(u.getFirstName(), u.getLastName(), u.getAddress());
            newUser.setUserId(UUID.randomUUID().toString());        
            instance.contentProvider.put(newUser.getUserId(), newUser);
            return newUser;
        } else {
            return null;
        }
    }

    public boolean updateUser(User u) {
        if (instance.contentProvider.containsKey(u.getUserId())) {
            instance.contentProvider.put(u.getUserId(), u);
            return true;
        }
        return false;
    }

    public boolean deleteUser(String userId) {
        if (instance.validUserId(userId)) {
            instance.contentProvider.remove(userId);
            return true;
        }
        return false;
    }

    public boolean validUserId(String userId) {
        return instance.contentProvider.containsKey(userId);
    }
}
