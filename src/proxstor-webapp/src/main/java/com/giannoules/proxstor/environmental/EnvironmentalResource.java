package com.giannoules.proxstor.environmental;

import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidEnvironmentalId;
import com.giannoules.proxstor.exception.EnvironmentalNotContainedWithinLocation;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EnvironmentalResource {

    private final String locId;
    private final String environmentalId;

    /*
     * all these Paths assume locId context, so stash in constructor
     */
    public EnvironmentalResource(String locId, String environmentalId) {
        this.locId = locId;
        this.environmentalId = environmentalId;
    }

    /*
     * update environmental environmentalId contained within Location locId
     * Note: environmentalId path must match environmentalId inside JSON (and be valid ID)
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)
     * failure - return 400 (Invalid Parameter) if environmentalId != Environmental.environmentalId,
     *           or if the environmentalId or locId are invalid
     * failure - return 500 (Internal Server Error) if the backend database isn't
     *           working
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putEnvironmentalLocIdEnvironmentalId(Environmental e) {
        if ((e.getEnvironmentalId() == null) || !environmentalId.equals(e.getEnvironmentalId())) {
            return Response.status(400).build();
        }
        try {
            if (EnvironmentalDao.instance.update(locId, e)) {
                return Response.noContent().build();
            } else {
                return Response.status(500).build();
            }
        } catch (InvalidEnvironmentalId | InvalidLocationId ex) {
            Logger.getLogger(EnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (EnvironmentalNotContainedWithinLocation ex) {
            Logger.getLogger(EnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

    /*
     * return the specified locId Locations's environmentalId Environmental
     * 
     * success - return 200 (Ok) and JSON representation Device
     * failure - return 404 (Not Found)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocationEnvironmental() {
        Environmental s;
        try {
            s = EnvironmentalDao.instance.getLocationEnvironmental(locId, environmentalId);
            return Response.ok().entity(s).build();
        } catch (InvalidEnvironmentalId | InvalidLocationId ex) {
            Logger.getLogger(EnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (EnvironmentalNotContainedWithinLocation ex) {
            Logger.getLogger(EnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

    /*
     * remove locId's Environmental environmentalId from Graph
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)     
     */
    @DELETE
    public Response deleteLocationEnvironmental() {
        try {
            if (EnvironmentalDao.instance.delete(locId, environmentalId)) {
                return Response.noContent().build();
            } else {
                return Response.status(500).build();
            }
        } catch (InvalidLocationId | InvalidEnvironmentalId ex) {
            Logger.getLogger(EnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (EnvironmentalNotContainedWithinLocation ex) {
            Logger.getLogger(EnvironmentalResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

}
