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

@Path("/users")
public class UsersResource {
  
    @Path("{userid: [0-9]*}")
    public UserResource getUserResource(@PathParam("userid") String userId) {
        return new UserResource(userId);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();        
        //users.addAll(UserDao.instance.getModel().values());
        users.addAll(UserDao.instance.getAllUsers());
        return users;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User postUser(User u) {
        /*
        if (!UserDao.instance.getModel().containsKey(u.getUserId())) {
            UserDao.instance.getModel().put(u.getUserId(), u);
            return "Success";
        }
        return "Duplicate";
                */
        return UserDao.instance.addUser(u);
    }  
    
}
