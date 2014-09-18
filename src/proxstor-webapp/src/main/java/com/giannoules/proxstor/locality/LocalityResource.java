package com.giannoules.proxstor.locality;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.exception.InvalidLocalityId;
import com.giannoules.proxstor.user.UserResource;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class LocalityResource {
     
    private final String localityId;

    /*
     * all these Paths assume a single localityId context, so stash in constructor
     */
    public LocalityResource(String localityId) {
        this.localityId = localityId;
    }
    
    /*
     * return the specified localityId Locality
     * 
     * success - return 200 (Ok) and JSON representation Locality
     * failure - return 404 (Not Found)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocality() {        
        Locality l;
        try {
            l = LocalityDao.instance.get(localityId);
            return Response.ok().entity(l).build();   
        } catch (InvalidLocalityId ex) {
            Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }             
    }
}
