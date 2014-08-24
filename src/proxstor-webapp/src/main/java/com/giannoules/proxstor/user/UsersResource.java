package com.giannoules.proxstor.user;

import com.giannoules.proxstor.knows.KnowsUserResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UsersResource {

    /*
     * returns all devices with identical descriptions
     */
    /*
    @Path("devices/{description}")
    @GET    
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Device> getAllSimilarDevices(@PathParam("description") String devDesc) {
        return DeviceDao.instance.getDevicesByDescription(devDesc);
    }
    */
     
    /*
     * returns all users system-wide!
     */
    /*
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated // on its way over to /api/search/users
    public Collection<User> getUsers() {
       return UserDao.instance.getAllUsers();        
    }
    */
    
    /*
     * returns all users matching criteria in partially expressed User JSON
     */
    /*
    @Path("search")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated // on its way over to /api/search/users
    public Collection<User> getMatchingUsers(User u) {
        return UserDao.instance.getMatchingUsers(u);        
    }    
    */
    
    /*
     * adds user to database
     *
     * returns instance of added User
     *
     * success - returns 201 (Created) with URI of new User and User JSON in the body
     * failure - returns 400 (Bad Request) if the User could not be added
     *           returns 500 (Server Error) if the User could was added and 
     *                                      URI building error occurred
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postUser(User in) {
        User u = UserDao.instance.addUser(in);
        if (u == null) {
            return Response.status(400).build();
        } else {
            try {
                URI createdUri = new URI("users/" + u.getUserId());
                return Response.created(createdUri).entity(u).build();
            } catch (URISyntaxException ex) {
                Logger.getLogger(UsersResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.serverError().build();
            }            
        }
    }
    
    // ---- BEGIN sub-resource locators ----
    
    /*
     * returns instance of UserResource to handle userId specific request 
     *
     * e.g. /api/users/123[/anything]
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
    
    
}
