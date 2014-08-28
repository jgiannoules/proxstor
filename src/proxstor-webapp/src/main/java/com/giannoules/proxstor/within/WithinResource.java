package com.giannoules.proxstor.within;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WithinResource {
    
    private final String locId;
    
    public WithinResource(String locId) {
        this.locId = locId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocationsWithin() {
        return null;
    }
    
    @Path("reverse")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContainingLocations() {
        return null;
    }
    
    @Path("{locid2: [0-9]+")
    public WithinLocationResource getWithinLocationResource(@PathParam("locid2") String locId2) {
        return new WithinLocationResource(locId, locId2);
    }
    
}
