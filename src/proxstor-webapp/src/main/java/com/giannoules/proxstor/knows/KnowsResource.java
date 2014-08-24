package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     *
     */ 
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnownUsers() {        
        Collection<User> users = KnowsDao.instance.getUserKnows(userId, strength);
        if (users == null) {
            return Response.noContent().build();
        }
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }
    
    /*
     * returns all Users who know userId with at least minimum strength
     */
    @Path("reverse")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnowsUsers() {
       Collection<User> users = KnowsDao.instance.getKnowsUser(userId, strength);
        if (users == null) {
            return Response.noContent().build();
        }
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }
    
    /*
     * establish that userId knows otherUser with strength
     */
    @Path("{otheruser: [0-9]+}")
    @POST    
    public Response establishUserKnows(@PathParam("otheruser") String otherUserId) {
        if (!KnowsDao.instance.addKnows(userId, otherUserId, strength)) {
            return Response.status(404).build();
        }
        URI createdUri;
        try {
            createdUri = new URI("/users/" + userId + "/knows/ " + strength + "/" + otherUserId);
            return Response.created(createdUri).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }
    
    /*
     * update the relationship that userId knows otherUser with strength
     */
    @Path("{otheruser: [0-9]+}")
    @PUT    
    public Response updateUserKnows(@PathParam("otheruser") String otherUserId) {
       return Response.noContent().build(); //@TODO 
    }
    
    /*
     * remove the relationship that userId knows otherUser. strength ignored
     */
    @Path("{otheruser: [0-9]+}")
    @PUT    
    public Response removeUserKnows(@PathParam("otheruser") String otherUserId) {
         if (KnowsDao.instance.removeKnows(userId, otherUserId)) {
            return Response.noContent().build();
        } else {
            return Response.status(404).build();
        }
    }
    
}
