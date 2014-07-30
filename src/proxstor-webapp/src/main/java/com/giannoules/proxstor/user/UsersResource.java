/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.giannoules.proxstor.user;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author James_Giannoules
 */
@Path("/users")
public class UsersResource {
  
    @Path("{userid}")
    public UserResource getUserResource(@PathParam("userid") String userId) {
        return new UserResource(userId);
    }    
    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();        
        users.addAll(UserDao.instance.getModel().values());
        return users;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String postUser(User u) {
        if (!UserDao.instance.getModel().containsKey(u.getUserId())) {
            UserDao.instance.getModel().put(u.getUserId(), u);
            return "Success";
        }
        return "Duplicate";
    }  
    
}
