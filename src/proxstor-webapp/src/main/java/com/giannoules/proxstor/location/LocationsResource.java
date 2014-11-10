package com.giannoules.proxstor.location;

import com.giannoules.proxstor.api.Location;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/location")
public class LocationsResource {

    /*
     * adds location to database
     *
     * returns instance of added Location
     *
     * success - returns 201 (Created) with URI of new Location and Location JSON in the body
     * failure - returns 400 (Bad Request) if the Location could not be added
     *           returns 500 (Server Error) if the Location could was added and 
     *                                      URI building error occurred
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postLocation(Location in) {
        Location l = LocationDao.instance.add(in);
        if (l == null) {
            return Response.status(400).build();
        } else {
            try {
                URI createdUri = new URI("locations/" + l.getLocId());
                return Response.created(createdUri).entity(l).build();
            } catch (URISyntaxException ex) {
                Logger.getLogger(LocationsResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.serverError().build();
            }
        }
    }

    // ---- BEGIN sub-resource locators ----
    
    /*
     * returns instance of LocationResource to handle locId specific request 
     */
    @Path("{locid}")
    public LocationResource getLocationResource(@PathParam("locid") String locId) {
        return new LocationResource(locId);
    }

}
