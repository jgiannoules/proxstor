package com.giannoules.proxstor.locality;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
import com.giannoules.proxstor.user.UserDao;
import com.giannoules.proxstor.user.UserResource;
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
     *  - if !manual mode, the sensorType must be valid and sensorValue must uniquely ID a sensor
     *  
     * The returned Locality will include:
     *  - localityId
     *  - active set to true
     *  - sensors[] updated to include sensorId of referenced sensorValue (if !manual mode)
     *  - arrive set
     *
     * returns Locality if the check-in can be associated with a device
     * returns 400 if the Locality can't be associated with a device
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postUser(Locality in) {
        System.out.println(in);
        Locality l;
        try {
            l = LocalityDao.instance.add(in);
            if (l == null) {
                return Response.status(400).build();
            }
            URI createdUri = new URI("locality/" + l.getLocalityId());
            return Response.created(createdUri).entity(l).build();

        } catch (InvalidLocationId | InvalidDeviceId | InvalidSensorId | SensorNotContainedWithinLocation ex) {
            Logger.getLogger(LocalitiesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(LocalitiesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();

        }
    }

    
    // ----> BEGIN sub-resource locators <----
    
    
    /*
     * returns instance of LocalityResource to handle localityId specific request 
     *
     * e.g. /api/locality/123[/anything]
     */
    @Path("{localityid: [0-9]+}")
    public LocalityResource getLocalityResource(@PathParam("localityid") String localityId) {
        return new LocalityResource(localityId);
    }

}
