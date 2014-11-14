package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.ProxStorUtil;
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
        try {
            if (!KnowsDao.instance.addKnows(userIdA, userIdB, strengthVal)) {
                return Response.status(500).build();
            }
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsStrengthResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (UserAlreadyKnowsUser ex) {
            Logger.getLogger(KnowsStrengthResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
        URI createdUri;
        try {
            createdUri = new URI("/users/" + ProxStorUtil.cleanPath(userIdA) + "/knows/" + strengthVal.toString() + "/" + ProxStorUtil.cleanPath(userIdB));
            return Response.created(createdUri).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(KnowsStrengthResource.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            if (KnowsDao.instance.updateKnows(userIdA, userIdB, strengthVal)) {
                return Response.noContent().build();
            }
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsStrengthResource.class.getName()).log(Level.SEVERE, null, ex);            
        }
        return Response.status(404).build();
    }   
}
