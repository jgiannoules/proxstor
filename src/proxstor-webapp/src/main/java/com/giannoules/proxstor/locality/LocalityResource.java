package com.giannoules.proxstor.locality;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.exception.InvalidLocalityId;
import com.giannoules.proxstor.user.UserResource;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
    
    /*
     * update locality
     * Note: localityId path must match localityId inside JSON (and be valid ID)
     * 
     * success - return 204 (No Content)
     * failure - return 400 (Invalid Parameter) if locationId != l.getLocationId()
     * failure - return 404 (Not Found)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putLocality(Locality l) {
        if ((l.getLocalityId() == null) || !l.getLocalityId().equals(localityId)) {
            return Response.status(400).build();
        }
        try {
            if (LocalityDao.instance.update(l)) {
                return Response.noContent().build();
            }
        } catch (InvalidLocalityId ex) {
            Logger.getLogger(LocalityResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();
    }

    /*
     * remove localityId from database
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @DELETE
    public Response deleteLocality() {
        try {
            if (LocalityDao.instance.delete(localityId)) {
                return Response.noContent().build();
            }
        } catch (InvalidLocalityId ex) {
            Logger.getLogger(LocalityResource.class.getName()).log(Level.SEVERE, null, ex);            
        }
        return Response.status(404).build();
    }

}
