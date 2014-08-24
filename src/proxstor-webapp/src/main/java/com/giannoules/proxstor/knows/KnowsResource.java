package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * handles answering requests regarding *who knows userId*
 */
public class KnowsResource {
    
    private final String userId;
    private final Float strength;
    
    public KnowsResource(String userId, Float strength) {
        this.userId = userId;
        this.strength = strength;
    }
    
    /*
     * return all Users userId knows with at least minimum strength
     */ 
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnownUsers() {
        ProxStorDebug.println("getKnownUsers(): " + userId + " min strength " + strength);
        return Response.ok().build();
    }
    
    /*
     * returns all Users who know userId with at least minimum strength
     */
    @Path("reverse")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnowsUsers() {
        ProxStorDebug.println("getKnowsUsers(): " + userId + " with min strength " + strength);
        return Response.ok().build();
    }
    
    /*
     * establish that userId knows otherUser with strength
     */
    @Path("{otheruser: [0-9]+}")
    @POST    
    public Response establishUserKnows(@PathParam("otheruser") String otherUserId) {
        ProxStorDebug.println("establishUserKnows(): " + userId + " --Knows [" + strength  + "]--> " + otherUserId);
        return Response.ok().build();
    }
    
    /*
     * update the relationship that userId knows otherUser with strength
     */
    @Path("{otheruser: [0-9]+}")
    @PUT    
    public Response updateUserKnows(@PathParam("otheruser") String otherUserId) {
        ProxStorDebug.println("updateUserKnows(): " + userId + " --Knows [" + strength  + "]--> " + otherUserId);
        return Response.ok().build();
    }
}
