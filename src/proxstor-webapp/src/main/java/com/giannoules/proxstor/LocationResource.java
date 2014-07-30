/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giannoules.proxstor;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author James_Giannoules
 */
@Path("/locations/{locid}")
public class LocationResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String getLocation(@PathParam("locid") String locId) {
        return "{ location " + locId + " coming your way }";
    }

    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String postLocation(@PathParam("locid") String locId) {
        return "{ updating location id " + locId + " }";
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String deleteLocation(@PathParam("locid") String locId) {
        return "{ deleting location id " + locId + " }";
    }
}
