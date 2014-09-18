package com.giannoules.proxstor.sensor;

import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SensorResource {

    private final String locId;
    private final String sensorId;

    /*
     * all these Paths assume locId context, so stash in constructor
     */
    public SensorResource(String locId, String sensorId) {
        this.locId = locId;
        this.sensorId = sensorId;
    }

    /*
     * update sensor sensorId contained within Location locId
     * Note: sensorId path must match sensorId inside JSON (and be valid ID)
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)
     * failure - return 400 (Invalid Parameter) if sensorId != Sensor.sensorId,
     *           or if the sensorId or locId are invalid
     * failure - return 500 (Internal Server Error) if the backend database isn't
     *           working
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putSensorLocIdSensorId(Sensor s) {
        if ((s.getSensorId() == null) || !sensorId.equals(s.getSensorId())) {
            return Response.status(400).build();
        }
        try {
            if (SensorDao.instance.update(locId, s)) {
                return Response.noContent().build();
            } else {
                return Response.status(500).build();
            }
        } catch (InvalidSensorId | InvalidLocationId ex) {
            Logger.getLogger(SensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (SensorNotContainedWithinLocation ex) {
            Logger.getLogger(SensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

    /*
     * return the specified locId Locations's sensorId Sensor
     * 
     * success - return 200 (Ok) and JSON representation Device
     * failure - return 404 (Not Found)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocationSensor() {
        Sensor s;
        try {
            s = SensorDao.instance.getLocationSensor(locId, sensorId);
            return Response.ok().entity(s).build();
        } catch (InvalidSensorId | InvalidLocationId ex) {
            Logger.getLogger(SensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (SensorNotContainedWithinLocation ex) {
            Logger.getLogger(SensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

    /*
     * remove locId's Sensor sensorId from Graph
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)     
     */
    @DELETE
    public Response deleteLocationSensor() {
        try {
            if (SensorDao.instance.delete(locId, sensorId)) {
                return Response.noContent().build();
            } else {
                return Response.status(500).build();
            }
        } catch (InvalidLocationId | InvalidSensorId ex) {
            Logger.getLogger(SensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (SensorNotContainedWithinLocation ex) {
            Logger.getLogger(SensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

}
