package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidEnvironmentalId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.EnvironmentalNotContainedWithinLocation;
import com.giannoules.proxstor.exception.UserAlreadyInLocation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CheckinDeviceEnvironmentalResource {

    private final String devId;

    public CheckinDeviceEnvironmentalResource(String devId) {
        this.devId = devId;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postCheckinDeviceEnvironmental(Environmental e) { 
        try {
            Locality l = CheckinDao.instance.deviceDetectEnvironmental(devId, e);
            if (l == null) {
                return Response.status(400).build();
            }
            URI createdUri = new URI("locality/" + l.getLocalityId());
            return Response.created(createdUri).entity(l).build();
        } catch (InvalidDeviceId | InvalidLocationId | InvalidEnvironmentalId | InvalidUserId | EnvironmentalNotContainedWithinLocation | UserAlreadyInLocation ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCheckinDeviceEnvironmental(Environmental e) {
        try {
            if (CheckinDao.instance.deviceUndetectEnvironmental(devId, e)) {
                return Response.noContent().build();
            }
        } catch (InvalidUserId | InvalidDeviceId | InvalidEnvironmentalId ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalIdResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();
    }

    @Path("{environmentalid}")
    public CheckinDeviceEnvironmentalIdResource getCheckinDeviceEnvironmentalIdResource(@PathParam("environmentalid") String environmentalId) {
        return new CheckinDeviceEnvironmentalIdResource(devId, environmentalId);
    }
}
