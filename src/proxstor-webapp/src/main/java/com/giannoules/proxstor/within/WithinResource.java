package com.giannoules.proxstor.within;

import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.api.Location;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        Collection<Location> locations;
        try {
            locations = WithinDao.instance.getWithin(locId);
            if (locations == null) {
                return Response.noContent().build();
            }
            return Response.ok((Location[]) locations.toArray(new Location[locations.size()])).build();
        } catch (InvalidLocationId ex) {
            Logger.getLogger(WithinResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    @Path("reverse")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContainingLocations() {
        Collection<Location> locations;
        try {
            locations = WithinDao.instance.getContaining(locId);
            if (locations == null) {
                return Response.noContent().build();
            }
            return Response.ok((Location[]) locations.toArray(new Location[locations.size()])).build();
        } catch (InvalidLocationId ex) {
            Logger.getLogger(WithinResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    @Path("location/{locid2}")
    public WithinLocationResource getWithinLocationResource(@PathParam("locid2") String locId2) {
        return new WithinLocationResource(locId, locId2);
    }

}
