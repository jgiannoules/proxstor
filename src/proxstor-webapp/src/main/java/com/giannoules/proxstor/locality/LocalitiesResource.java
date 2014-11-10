package com.giannoules.proxstor.locality;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.checkin.CheckinDao;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidEnvironmentalId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.EnvironmentalNotContainedWithinLocation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * Locality represents a period of time that a Device (and thus a User) is
 * within sensing range of a Environmental (and thus in a Location). In the abstract
 * the /locality (and com.giannoules.proxstor.locality) provide only the 
 * proxstor 'object' store/retrieval/update/delete. The full functionality is
 * achieved in concert with com.giannoules.proxstor.checkin. Thus these
 * interfaces in a production system would be consumed internally only. They
 * are provided here for development and debug purposes.
 */

@Path("/locality")
public class LocalitiesResource {

    /*
     * add a new Locality to the database. 
     *
     * The client @POSTs a partial Locality to indicate what environmental
     * values have been sensed.
     *
     * The minimum information inside the Locality:
     *  - devId must be a valid device
     *  - if manual mode, the locId must be a valid location
     *  - if !manual mode, the environmentalType must be valid and environmentalValue must uniquely ID a environmental
     *  
     * The returned Locality will include:
     *  - localityId
     *  - active set to true
     *  - environmentals[] updated to include environmentalIds of referenced environmentalValue (if !manual mode)
     *  - arrive set
     *
     * returns Locality if the check-in can be associated with a device
     * returns 400 if the Locality can't be associated with a device
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postUser(Locality in) {
        Locality l;
        try {
            l = LocalityDao.instance.add(in);
            if (l == null) {
                return Response.status(400).build();
            }
            URI createdUri = new URI("locality/" + l.getLocalityId());
            return Response.created(createdUri).entity(l).build();

        } catch (InvalidLocationId | InvalidDeviceId | InvalidEnvironmentalId | EnvironmentalNotContainedWithinLocation ex) {
            Logger.getLogger(LocalitiesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(LocalitiesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();

        }
    }

    
    @Path("user/{userid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserLocalities(@PathParam("userid") String userId) {
        Collection<Locality> localities;
        try {
            localities = CheckinDao.instance.getPreviousLocalities(userId, 1024); // @TODO set depth?
            if (localities.isEmpty()) {
                return Response.noContent().build();
            }
            /*
             * ok() will not take Collection directly, so convert to array
             */
            return Response.ok((Locality[]) localities.toArray(new Locality[localities.size()])).build();
        } catch (InvalidUserId ex) {
            Logger.getLogger(LocalitiesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    // ----> BEGIN sub-resource locators <----
    
    
    /*
     * returns instance of LocalityResource to handle localityId specific request 
     *
     * e.g. /api/locality/123[/anything]
     */
    @Path("{localityid}")
    public LocalityResource getLocalityResource(@PathParam("localityid") String localityId) {
        return new LocalityResource(localityId);
    }

}
