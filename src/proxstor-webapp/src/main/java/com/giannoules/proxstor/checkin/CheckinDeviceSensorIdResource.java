package com.giannoules.proxstor.checkin;

import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
import com.giannoules.proxstor.exception.UserAlreadyInLocation;
import com.giannoules.proxstor.locality.LocalityDao;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CheckinDeviceSensorIdResource {

    private final String devId;
    private final String sensorId;

    public CheckinDeviceSensorIdResource(String devId, String sensorId) {
        this.devId = devId;
        this.sensorId = sensorId;
    }

    /*
     * devId detects sensorId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDeviceDetectSensor() {
        Sensor partial = new Sensor();
        partial.setSensorId(sensorId);
        try {            
            Locality l = CheckinDao.instance.deviceDetectSensor(devId, partial);
            if (l == null) {
                return Response.status(400).build();
            }
            URI createdUri = new URI("locality/" + l.getLocalityId());
            return Response.created(createdUri).entity(l).build();
        } catch (InvalidDeviceId | InvalidLocationId | InvalidSensorId | InvalidUserId | SensorNotContainedWithinLocation | UserAlreadyInLocation ex) {
            Logger.getLogger(CheckinDeviceSensorIdResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(CheckinDeviceSensorIdResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }

    /*
     * devId un-detects sensorId
     */
    @DELETE
    public Response deleteDeviceUndetectSensor() {
        Sensor partial = new Sensor();
        partial.setSensorId(sensorId);
        try {
            if (CheckinDao.instance.deviceUndetectSensor(devId, partial)) {
                return Response.noContent().build();
            }
        } catch (InvalidUserId | InvalidDeviceId | InvalidSensorId ex) {
            Logger.getLogger(CheckinDeviceSensorIdResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(404).build();
    }

}
