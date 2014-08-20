package com.giannoules.proxstor.user;

import com.giannoules.proxstor.knows.KnowsResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
   
    @Path("/knows")
    public KnowsResource getKnowsResource() {
        return new KnowsResource(userId);
    }    
       
}
