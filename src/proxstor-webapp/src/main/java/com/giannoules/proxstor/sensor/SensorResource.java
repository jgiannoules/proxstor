package com.giannoules.proxstor.sensor;

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
     * sucess - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putSensorLocIdSensorId(Sensor s) {
        if ((s.getSensorId() != null) && sensorId.equals(s.getSensorId())) {
            if (SensorDao.instance.updateLocationSensor(locId, s)) {
                return Response.noContent().build();
            }
        }
        return Response.status(404).build();
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
        Sensor s = SensorDao.instance.getLocationSensor(locId, sensorId);
        if (s == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(s).build();
    }

    /*
     * remove locId's Sensor sensorId from Graph
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)     
     */
    @DELETE
    public Response deleteLocationSensor() {
        if (SensorDao.instance.deleteLocationSensor(locId, sensorId)) {
            return Response.noContent().build();
        } else {
            return Response.status(404).build();
        }
    }

}
