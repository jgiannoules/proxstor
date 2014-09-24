package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
import com.giannoules.proxstor.exception.UserAlreadyInLocation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CheckinDeviceSensorResource {

    private final String devId;

    public CheckinDeviceSensorResource(String devId) {
        this.devId = devId;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postCheckinDeviceSensor(Sensor s) { 
        try {
            Locality l = CheckinDao.instance.deviceDetectSensor(devId, s);
            if (l == null) {
                return Response.status(400).build();
            }
            URI createdUri = new URI("locality/" + l.getLocalityId());
            return Response.created(createdUri).entity(l).build();
        } catch (InvalidDeviceId | InvalidLocationId | InvalidSensorId | InvalidUserId | SensorNotContainedWithinLocation | UserAlreadyInLocation ex) {
            Logger.getLogger(CheckinDeviceSensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckinDeviceSensorResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCheckinDeviceSensor(Sensor s) {
        try {
            if (CheckinDao.instance.deviceUndetectSensor(devId, s)) {
                return Response.noContent().build();
            }
        } catch (InvalidUserId | InvalidDeviceId | InvalidSensorId ex) {
            Logger.getLogger(CheckinDeviceSensorIdResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();
    }

    @Path("{sensorid}")
    public CheckinDeviceSensorIdResource getCheckinDeviceSensorIdResource(@PathParam("sensorid") String sensorId) {
        return new CheckinDeviceSensorIdResource(devId, sensorId);
    }
}
