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
@Path("/users/{userid}/devices/{devid}")
public class DeviceResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String getDevice(
            @PathParam("userid") String userId,
            @PathParam("devid") String devId) {
        return "{ userid " + userId + "'s device " + devId + " coming your way }";
    }

    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String postDevice(
            @PathParam("userid") String userId,
            @PathParam("devid") String devId) {
        return "{ updating user " + userId + "'s device id " + devId + " }";
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String deleteDevice(
            @PathParam("userid") String userId,
            @PathParam("devid") String devId) {
        return "{ deleting user " + userId + "'s device id " + devId + " }";
    }
}
