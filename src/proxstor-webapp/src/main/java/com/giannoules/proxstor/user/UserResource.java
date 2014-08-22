package com.giannoules.proxstor.user;

import com.giannoules.proxstor.device.DeviceResource;
import com.giannoules.proxstor.knows.UserKnowsResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

public class UserResource {
    
    private final String userId;
    
    /*
     * all these Paths assume userId context, so stash in constructor
     */
    public UserResource(String userId) {
        this.userId = userId;
    }
    
    /*
     * return DeviceResource handler for specified user
     */
    @Path("devices")
    public DeviceResource getDeviceResource() {
        return new DeviceResource(userId);
    }
    
    /*
     * return KnowResource handler for specified user
     */
    @Path("knows")
    public UserKnowsResource getKnowsResource() {
        return new UserKnowsResource(userId);
    } 
    
    /*
     * return the specified userId User
     * if the userId is invalid throw 404
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser() {
        User u = UserDao.instance.getUser(userId);
        if (u == null){             
            throw new WebApplicationException(404);
        }
        return u;
    }
    
    /*
     * update user
     * userId path must match userId inside JSON (and be valid ID)
     * if not, return false
     */
    @PUT    
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean putUser(User u) {
        if ((u.getUserId() != null) && u.getUserId().equals(userId)) {
            return UserDao.instance.updateUser(u);            
        }
        return false;
    }
    
    /*
     * remove userId from database
     *
     * if userId is not valid user return false
     */
    @DELETE
    public boolean deleteUser() {
        return UserDao.instance.deleteUser(userId);
    }
    
}
