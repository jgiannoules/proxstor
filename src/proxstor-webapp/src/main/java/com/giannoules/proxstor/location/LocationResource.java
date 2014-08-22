package com.giannoules.proxstor.location;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class LocationResource {

    private String locId;
    
    public LocationResource(String locId) {
        this.locId = locId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getLocation() {
        return "{ location " + locId + " coming your way }";
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String postLocation() {
        return "{ updating location id " + locId + " }";
    }

    @DELETE    
    public String deleteLocation() {
        return "{ deleting location id " + locId + " }";
    }
}
