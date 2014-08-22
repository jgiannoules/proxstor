package com.giannoules.proxstor.sensor;

import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sensors")
public class SensorsResource {

    /*
     * returns specified sensorid Sensor, otherwise 404
     * @TODO fix me
     */
    @Path("/{sensorid: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sensor getSensor(@PathParam("sensorid") String sensorId) {
        return SensorDao.instance.getSensorById(sensorId);
    }

    /*
     * returns all devices system-wide!
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Sensor> getSensors() {
        return SensorDao.instance.getAllSensors();
    }

}
