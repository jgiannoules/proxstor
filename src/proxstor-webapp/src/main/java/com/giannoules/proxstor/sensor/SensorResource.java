package com.giannoules.proxstor.sensor;

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SensorResource {

    private final String locId;

    /*
     * all these Paths assume locId context, so stash in constructor
     */
    public SensorResource(String locId) {
        this.locId = locId;        
    }
    
    /*
     * all of locId's devices
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Sensor> getSensor() {
        return SensorDao.instance.getAllLocationSensors(locId);
    }

   /*
     * locId adding new Sensor
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)    
    public Sensor postSensor(Sensor s) {
        return SensorDao.instance.addLocationSensor(locId, s);
    }

    /*
     * locId updating sensor
     * return 304 if
     *   - sensor not found
     *   - location doesn't contain device
     *   - Sensor sen sorId doesn't match sensorid @PathParam
     */
    @Path("{sensorid: [0-9]+}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)    
    public Response putSensorSensorId(@PathParam("sensorid") String sensorId, Sensor s) {
        if ((s.getSensorId() != null) && sensorId.equals(s.getSensorId()) 
                && SensorDao.instance.updateLocationSensor(locId, s)) {
            return Response.ok().build();
        }
        return Response.notModified().build();
    }
    
    /*
     * retrieve locId's Sensor with sensorId
     * return 404 if not found
     */
    @Path("{sensorid: [0-9]+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensor(@PathParam("sensorid") String sensorId) {
        Sensor s = SensorDao.instance.getLocationSensor(locId, sensorId);
        if (s == null) {
            throw new WebApplicationException(404);
        }
        return Response.ok(s).build(); 
    }
    
    /*
     * remove locID's Sensor sensorId from Graph
     */
    @Path("{sensorid: [0-9]+}")
    @DELETE
    public boolean deleteSensor(@PathParam("sensorid") String sensorId) {
        return SensorDao.instance.deleteLocationSensor(locId, sensorId);
    }
}

