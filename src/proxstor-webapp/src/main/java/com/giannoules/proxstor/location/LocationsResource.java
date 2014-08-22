package com.giannoules.proxstor.location;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/locations")
public class LocationsResource {

    @Path("{locid: [0-9]+}")
    public LocationResource getLocationResource(@PathParam("locid") String locId) {
        return new LocationResource(locId);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String getLocations() {
        return "{ all locations coming your way }";
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String postLocation() {
        return "{ adding location }";
    }
}
