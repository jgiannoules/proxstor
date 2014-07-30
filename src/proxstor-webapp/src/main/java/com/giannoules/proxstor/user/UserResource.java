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
        User u = UserDao.instance.getModel().get(userId);
        return u;
    }
    
    @PUT    
    @Consumes(MediaType.APPLICATION_JSON)
    public String putUser(User u) {    
        if (UserDao.instance.getModel().containsKey(userId) &&
                u.getUserId().equals(userId)) {
            UserDao.instance.getModel().put(u.userId, u);
            return "Success";
        }
        return "Fail";
    }
    
    @DELETE    
    public String deleteUser() {        
        if (UserDao.instance.getModel().containsKey(userId)) {
            UserDao.instance.getModel().remove(userId);
            return "Success";
        }
        return "Fail";
    }
}
