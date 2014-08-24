package com.giannoules.proxstor.location;

import com.giannoules.proxstor.sensor.SensorResource;
import com.giannoules.proxstor.sensor.SensorsResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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
        Location l = LocationDao.instance.getLocationById(locId);
        if (l == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(l).build();
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
        if ((l.getLocId() != null) && l.getLocId().equals(locId)) {
            if (LocationDao.instance.updateLocation(l)) {
                return Response.noContent().build();
            }
        }
        return Response.status(404).build();
    }

    /*
     * remove locId from database
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @DELETE
    public Response deleteLocation() {
        if (LocationDao.instance.deleteLocation(locId)) {
            return Response.noContent().build();
        } else {
            return Response.status(404).build();
        }
    }

    // ---- BEGIN sub-resource locators
    
    /*
     * return SensorResource handler for this Location
     */
    @Path("sensors")
    public SensorsResource getSensorResource() {
        return new SensorsResource(locId);
    }

}
