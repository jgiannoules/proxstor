package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.exception.InvalidUserId;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class KnowsResource {
    
    private final String userIdA;        // always within context of a user    
    
    public KnowsResource(String userId) {
        ProxStorDebug.println("KnowsResource constructor " + userId);
        this.userIdA = userId;        
    }
    
    @Path("strength/{strength: [0-9]{1,2}|100}")
    public KnowsStrengthResource getKnowsResource(@PathParam("strength") Integer strength) {
        return new KnowsStrengthResource(userIdA, strength);
    }
    
    /*
     * remove the relationship that userId knows otherUser
     *
     * returns 204 (No Content) when successful
     * returns 404 (Not Found) if relationship was not already established or
     *                         the userIds are simply invalid
     */    
    @Path("user/{userid}")
    @DELETE
    public Response removeUserKnows(@PathParam("userid") String userIdB) {
        ProxStorDebug.println("removeUserKnows");
        try {
            if (KnowsDao.instance.removeKnows(userIdA, userIdB)) {
                return Response.noContent().build();
            }
        } catch (InvalidUserId ex) {
            Logger.getLogger(KnowsStrengthResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();        
    }
        
}
