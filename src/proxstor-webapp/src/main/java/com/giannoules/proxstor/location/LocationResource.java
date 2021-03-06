package com.giannoules.proxstor.location;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.knows.NearbyResource;
import com.giannoules.proxstor.environmental.EnvironmentalsResource;
import com.giannoules.proxstor.within.WithinResource;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class LocationResource {

    private final String locId;

    public LocationResource(String locId) {
        this.locId = locId;
    }
    
    /*
     * return the specified locId Location
     * 
     * success - return 200 (Ok) and JSON representation Location
     * failure - return 404 (Not Found)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocation() {
        Location l;
        try {
            l = LocationDao.instance.get(locId);
            return Response.ok().entity(l).build();
        } catch (InvalidLocationId ex) {
            Logger.getLogger(LocationResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    /*
     * update location
     * Note: locId path must match locId inside JSON (and be valid ID)
     * 
     * sucess - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putLocation(Location l) {
        if ((l.getLocId() == null) || !l.getLocId().equals(locId)) {
            return Response.status(400).build();
        }
        try {
            LocationDao.instance.update(l);
            return Response.noContent().build();
        } catch (InvalidLocationId ex) {
            Logger.getLogger(LocationResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    /*
     * remove locId from database
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @DELETE
    public Response deleteLocation() {
        try {
            if (LocationDao.instance.delete(locId)) {
                return Response.noContent().build();
            }
        } catch (InvalidLocationId ex) {
            Logger.getLogger(LocationResource.class.getName()).log(Level.SEVERE, null, ex);            
        }
        return Response.status(404).build();
    }

    // ---- BEGIN sub-resource locators
    
    /*
     * return EnvironmentalResource handler for this Location
     */
    @Path("environmental")
    public EnvironmentalsResource getEnvironmentalResource() {
        return new EnvironmentalsResource(locId);
    }
    
    /*
     * return WithinResource handle for this location
     */
    @Path("within")
    public WithinResource getWithinResource() {
        return new WithinResource(locId);
    }
    
    /*
     * return WithinResource handle for this location
     */
    // @TODO: regex this correctly for doubles (prev regex was flawed)
    @Path("nearby/distance/{distance}")
    public NearbyResource getNearbyResource(@PathParam("distance") Double distance) {
        return new NearbyResource(locId, distance);
    }

}
