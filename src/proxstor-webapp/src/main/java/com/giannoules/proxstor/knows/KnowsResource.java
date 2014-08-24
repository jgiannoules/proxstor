package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
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
    private final Integer strength;
    
    public KnowsResource(String userId, Integer strength) {
        this.userId = userId;
        this.strength = strength;
    }
    
    /*
     * return all Users userId knows with at least minimum strength
     *
     * returns 204 (No Content) if no knows relatioships match
     * returns 200 (Ok) with array of Users if matches found
     * returns 404 (Not Found) if the userID is invalid
     */ 
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnownUsers() {
        ProxStorDebug.println("getKnowsUsers()");
        if (!UserDao.instance._validUserId(userId)) {
            return Response.status(404).build();
        }
        Collection<User> users = KnowsDao.instance.getUserKnowsStrength(userId, strength);
        if (users == null) {
            return Response.noContent().build();
        }
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }
    
    /*
     * returns all Users who know userId with at least minimum strength
     *
     * returns 204 (No Content) if no knows relatioships match
     * returns 200 (Ok) with array of Users if matches found
     * returns 404 (Not Found) if the userID is invalid
     */
    @Path("reverse")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnowsUsers() {
       ProxStorDebug.println("getKnowsUsers()");
        if (!UserDao.instance._validUserId(userId)) {
            return Response.status(404).build();
        }
       Collection<User> users = KnowsDao.instance.getKnowsUserStrength(userId, strength);
        if (users == null) {
            return Response.noContent().build();
        }
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }
    
    /*
     * establish that userId knows otherUser with strength
     *
     * success - returns 201 (Created) with URI of new Knows relationship
     * failure - returns 404 (Not Found) if either userID is invalid
     *           returns 400 (Bad Request) if the Knows could not be established (or is already established @TODO)
     *           returns 500 (Server Error) if the Knows could be established 
     *                                      but URI building error occurred
     */
    @Path("{otheruser: [0-9]+}")
    @POST    
    public Response establishUserKnows(@PathParam("otheruser") String otherUserId) {
        ProxStorDebug.println("establishUserKnows()");
        if (!UserDao.instance._validUserId(userId) || !UserDao.instance._validUserId(otherUserId)) {
            return Response.status(404).build();
        }
        if (!KnowsDao.instance.addKnows(userId, otherUserId, strength)) {
            return Response.status(400).build();
        }
        URI createdUri;
        try {
            createdUri = new URI("/users/" + userId + "/knows/" + strength.toString() + "/" + otherUserId);            
            return Response.created(createdUri).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }
    
    /*
     * update that userId knows otherUser with strength
     *
     * success - returns 204 (No Content)
     * failure - returns 404 (Not Found) if either userID is invalid
     *           returns 400 (Bad Request) if the Knows could not be established (or is already established @TODO)
     *           returns 500 (Server Error) if the Knows could be established 
     *                                      but URI building error occurred
     */
    @Path("{otheruser: [0-9]+}")
    @PUT    
    public Response updateUserKnows(@PathParam("otheruser") String otherUserId) {        
       ProxStorDebug.println("updateUserKnows()");
       return Response.status(501).build();
       /*
        if (!UserDao.instance._validUserId(userId) || !UserDao.instance._validUserId(otherUserId)) {
            return Response.status(404).build();
        }
        if (!KnowsDao.instance.updateKnows(userId, otherUserId, strength)) {
            return Response.status(400).build();
        }
        return Response.noContent().build();
       */
    }
    
    /*
     * remove the relationship that userId knows otherUser. strength ignored
     *
     * returns 204 (No Content) when sucessful
     * returns 404 (Not Found) if relationship was not already established
     */
    @Path("{otheruser: [0-9]+}")
    @DELETE
    public Response removeUserKnows(@PathParam("otheruser") String otherUserId) {
         ProxStorDebug.println("removeUserKnows()");
         if (KnowsDao.instance.removeKnows(userId, otherUserId)) {
            return Response.noContent().build();
        } else {
            return Response.status(404).build();
        }
    }
    
}
