package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.exception.InvalidUserId;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CheckinUserResource {

    private final String userId;

    public CheckinUserResource(String userId) {
        this.userId = userId;
    }
    
    @Path("location/{locid}")
    public CheckinUserLocationResource getCheckinUserLocationResource(@PathParam("locid") String locId) {
        return new CheckinUserLocationResource(userId, locId);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckinUser() {
        try {
            Locality l = CheckinDao.instance.getCurrentLocality(userId);
            if (l == null) {
                return Response.noContent().build();
            }
            return Response.ok(l).build();         
        } catch (InvalidUserId ex) {
            Logger.getLogger(CheckinUserResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();
    }
}
