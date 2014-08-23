package com.giannoules.proxstor.location;

import com.giannoules.proxstor.ProxStorGraphDatabaseNotRunningException;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/locations")
public class LocationsResource {
    
    /*
     * returns instance of LocationResource to handle locId specific request 
     */
    @Path("{locid: [0-9]+}")
    public LocationResource getLocationResource(@PathParam("locid") String locId) {
        return new LocationResource(locId);
    }
    
    /*
     * returns all locations system-wide!
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Collection<Location> getLocations() {
        return LocationDao.instance.getAllLocations();
    }

    /*
     * adds location to database
     *
     * returns instance of added Location
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Location postLocation(Location l) {
        return LocationDao.instance.addLocation(l);
    }
}
