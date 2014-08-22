package com.giannoules.proxstor.sensor;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sensors/{sensorid}")
public class SensorResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String getSensor(@PathParam("sensorid") String sensorId) {
        return "{ sensor " + sensorId + " coming your way }";
    }

    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String postSensor(@PathParam("sensorid") String sensorId) {
        return "{ updating sensor id " + sensorId + " }";
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String deleteSensor(@PathParam("sensorid") String sensorId) {
        return "{ deleting sensor id " + sensorId + " }";
    }
}
