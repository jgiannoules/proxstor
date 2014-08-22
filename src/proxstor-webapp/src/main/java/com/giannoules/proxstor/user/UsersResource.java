package com.giannoules.proxstor.user;

import com.giannoules.proxstor.knows.KnowsUserResource;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/users")
public class UsersResource {
  
    /*
     * returns instance of UserResource to handle userId specific request 
     */
    @Path("{userid: [0-9]+}")
    public UserResource getUserResource(@PathParam("userid") String userId) {
        return new UserResource(userId);
    }
    
    /*
     * returns instance of KnowsUserResource to handle queries about who
     * knows userId
     */
    @Path("knows/{userid: [0-9]+}")
    public KnowsUserResource getKnowsUserResource(@PathParam("userid") String userId) {
        return new KnowsUserResource(userId);
    }
    
    /*
     * returns all users system-wide!
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<User> getUsers() {
       return UserDao.instance.getAllUsers();        
    }
    
    /*
     * adds user to database
     *
     * returns instance of added User
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User postUser(User u) {      
        return UserDao.instance.addUser(u);
    }  
    
}
