package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.UserAlreadyInLocation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

public class CheckinUserLocationResource {
    private final String userId;
    private final String locId;

    public CheckinUserLocationResource(String userId, String locId) {
        this.userId = userId;
        this.locId = locId;
    }
    
    /**
     * 
     * @return 
     */
    @POST
    public Response getCheckinUserLocationResource() {
        try {
            Locality l = CheckinDao.instance.setUserLocation(userId, locId);
            if (l == null) {
                return Response.status(400).build();
            }
            URI createdUri = new URI("locality/" + l.getLocalityId());
            return Response.created(createdUri).entity(l).build();
        } catch (InvalidLocationId | InvalidUserId | UserAlreadyInLocation ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckinDeviceEnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }
    
    /**
     * 
     * @return 
     */
    @DELETE
    public Response deleteCheckoutUserLocationResource() {
        try {
            if (CheckinDao.instance.unsetUserLocation(userId, locId)) {
                return Response.noContent().build();
            }
        } catch (InvalidLocationId | InvalidUserId ex) {
            Logger.getLogger(CheckinUserLocationResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();
    }
}
