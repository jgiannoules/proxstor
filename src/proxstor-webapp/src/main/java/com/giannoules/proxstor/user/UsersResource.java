package com.giannoules.proxstor.user;

import com.giannoules.proxstor.api.User;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UsersResource {
    
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
        User u = UserDao.instance.add(in);
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
    
    // ----> BEGIN sub-resource locators <----
    
    /*
     * returns instance of UserResource to handle userId specific request 
     *
     * e.g. /api/users/123[/anything]
     *
     * if the userId is not valid throw exception (performance optimization)
     */
    @Path("{userid: [0-9]+}")
    public UserResource getUserResource(@PathParam("userid") String userId) {
        if (UserDao.instance.valid(userId)) {
            return new UserResource(userId);
        } else {
            throw new WebApplicationException();
        }
    }
    
}
