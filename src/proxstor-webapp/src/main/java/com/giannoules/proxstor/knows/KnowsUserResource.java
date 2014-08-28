package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.UserAlreadyKnowsUser;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class KnowsUserResource {
    private final String userIdA;
    private final String userIdB;
    private final Integer strengthVal;

    public KnowsUserResource(String userIdA, String userIdB, Integer strengthVal) {
        this.userIdA = userIdA;
        this.userIdB = userIdB;
        this.strengthVal = strengthVal;
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
    @POST
    public Response establishUserKnows() {
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
    @PUT
    public Response updateUserKnows() {
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
