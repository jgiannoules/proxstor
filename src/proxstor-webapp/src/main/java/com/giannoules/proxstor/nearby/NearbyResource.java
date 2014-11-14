package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.nearby.NearbyDao;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * handles all URI requests regarding 'nearby' relationship between locations
 */
public class NearbyResource {

    private final String locIdA;        // always within context of a location
    private final Double distanceVal;  // distance meaning varies by request

    public NearbyResource(String locId, Double distance) {
        this.locIdA = locId;
        this.distanceVal = distance;
    }

    /*
     * return all Locations involving locId with distance >= distanceVal
     *
     * returns 200 (Ok) with array of Location if matches found
     * returns 204 (No Content) if no nearby relationships match criteria
     * returns 404 (Not Found) if locId is invalid
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNearbylocations() {
        Collection<Location> locations;
        try {
            locations = NearbyDao.instance.getLocationsNearby(locIdA, distanceVal, 1024); // max 1024 locations returned
        } catch (InvalidLocationId ex) {
            Logger.getLogger(KnowsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
        if (locations == null) {
            return Response.noContent().build();
        }
        return Response.ok((Location[]) locations.toArray(new Location[locations.size()])).build();
    }

    @Path("location/{otherloc}")
    public NearbyLocationResource returnNearbyLocationResource(@PathParam("otherloc") String otherLoc) {
        return new NearbyLocationResource(locIdA, otherLoc, distanceVal);
    }
    
}
