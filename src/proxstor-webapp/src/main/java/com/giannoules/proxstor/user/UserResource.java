/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.giannoules.proxstor.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author James_Giannoules
 */
public class UserResource {
    
    private String userId;
    
    public UserResource(String userId) {
        this.userId = userId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser() {
        User u = UserDao.instance.getUser(userId);
        return u;
    }
    
    @PUT    
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean putUser(User u) {
        if ((u.getUserId() != null) && u.getUserId().equals(userId)) {
            return UserDao.instance.updateUser(u);            
        }
        return false;
    }
    
    @DELETE
    public boolean deleteUser() {
        return UserDao.instance.deleteUser(userId);
    }
}
