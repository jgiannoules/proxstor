package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.UserAlreadyKnowsUser;
import com.giannoules.proxstor.user.User;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
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
 * handles all URI requests regarding 'knows' relationship between users
 */
public class KnowsResource {

    private final String userIdA;        // always within context of a user
    private final Integer strengthVal;  // strength meaning varies by request

    public KnowsResource(String userId, Integer strength) {
        this.userIdA = userId;
        this.strengthVal = strength;
    }

    /*
     * return all Users which userId knows with strength >= strengthVal
     *
     * returns 200 (Ok) with array of Users if matches found
     * returns 204 (No Content) if no knows relationships match criteria
     * returns 404 (Not Found) if userId is invalid
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnownUsers() {
        ProxStorDebug.println("getKnowsUsers()");
        Collection<User> users;
        try {
            users = KnowsDao.instance.getUserKnows(userIdA, strengthVal, OUT, 1024); // max 1024 users returned
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
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
     * returns 404 (Not Found) if userId is invalid
     */
    @Path("reverse")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKnowsUsers() {
        ProxStorDebug.println("getKnowsUsers()");
        Collection<User> users;
        try {
            users = KnowsDao.instance.getUserKnows(userIdA, strengthVal, IN, 1024); // max 1024 users returned
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
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
     *           returns 400 (Bad Request) if the Knows is already established
     *           returns 500 (Server Error) if the Knows could be established 
     *                                      but URI building error occurred
     */
    @Path("{otheruser: [0-9]+}")
    @POST
    public Response establishUserKnows(@PathParam("otheruser") String userIdB) {
        ProxStorDebug.println("establishUserKnows()");
        try {
            if (!KnowsDao.instance.addKnows(userIdA, userIdB, strengthVal)) {
                return Response.status(500).build();
            }
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (UserAlreadyKnowsUser ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
        URI createdUri;
        try {
            createdUri = new URI("/users/" + userIdA + "/knows/" + strengthVal.toString() + "/" + userIdB);
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
     *           returns 400 (Bad Request) if the knows relationship was not already established
     */
    @Path("{otheruser: [0-9]+}")
    @PUT
    public Response updateUserKnows(@PathParam("otheruser") String userIdB) {
        ProxStorDebug.println("updateUserKnows()");        
        try {
            if (KnowsDao.instance.updateKnows(userIdA, userIdB, strengthVal)) {
                return Response.noContent().build();
            } else {
                return Response.status(400).build();
            }
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }        
    }

    /*
     * remove the relationship that userId knows otherUser
     * note that strength value is ignored
     *
     * returns 204 (No Content) when successful
     * returns 404 (Not Found) if relationship was not already established or
     *                         the userIds are simply invalid
     */
    @Path("{otheruser: [0-9]+}")
    @DELETE
    public Response removeUserKnows(@PathParam("otheruser") String userIdB) {
        ProxStorDebug.println("removeUserKnows()");        
        try {
            KnowsDao.instance.removeKnows(userIdA, userIdB);
            return Response.noContent().build();
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();        
    }

}
