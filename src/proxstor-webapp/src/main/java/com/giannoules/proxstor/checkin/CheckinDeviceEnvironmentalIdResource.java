package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.ProxStorDebug;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidEnvironmentalId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.EnvironmentalNotContainedWithinLocation;
import com.giannoules.proxstor.exception.UserAlreadyInLocation;
import com.giannoules.proxstor.locality.LocalityDao;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CheckinDeviceEnvironmentalIdResource {

    private final String devId;
    private final String environmentalId;
            
    public CheckinDeviceEnvironmentalIdResource(String devId, String environmentalId) {
        this.devId = devId;
        this.environmentalId = environmentalId;
    }

    /*
     * devId detects environmentalId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDeviceDetectEnvironmental() {
        long start = ProxStorDebug.startTimer();
        Environmental partial = new Environmental();
        partial.setEnvironmentalId(environmentalId);
        try {            
            Locality l = CheckinDao.instance.deviceDetectEnvironmental(devId, partial);
            if (l == null) {
                return Response.status(400).build();
            }
            URI createdUri = new URI("locality/" + l.getLocalityId());
            ProxStorDebug.endTimer("postDeviceDetectEnvironmental", start);
            return Response.created(createdUri).entity(l).build();
        } catch (InvalidDeviceId | InvalidLocationId | InvalidEnvironmentalId | InvalidUserId | EnvironmentalNotContainedWithinLocation | UserAlreadyInLocation ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalIdResource.class.getName()).log(Level.SEVERE, null, ex);
            ProxStorDebug.endTimer("postDeviceDetectEnvironmental400", start);
            return Response.status(400).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalIdResource.class.getName()).log(Level.SEVERE, null, ex);
            ProxStorDebug.endTimer("postDeviceDetectEnvironmental500", start);
            return Response.serverError().build();
        }
    }

    /*
     * devId un-detects environmentalId
     */
    @DELETE
    public Response deleteDeviceUndetectEnvironmental() {        
        long start = ProxStorDebug.startTimer();
        Environmental partial = new Environmental();
        partial.setEnvironmentalId(environmentalId);
        try {
            if (CheckinDao.instance.deviceUndetectEnvironmental(devId, partial)) {
                ProxStorDebug.endTimer("deleteDeviceUndetectEnvironmental", start);
                return Response.noContent().build();
            }
        } catch (InvalidUserId | InvalidDeviceId | InvalidEnvironmentalId ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalIdResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        ProxStorDebug.endTimer("deleteDeviceUndetectEnvironmental404", start);
        return Response.status(404).build();
    }

}
