package com.giannoules.proxstor.sensor;

import com.giannoules.proxstor.location.LocationDao;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SensorsResource {

    private final String locId;

    /*
     * all these Paths assume locId context, so stash in constructor
     */
    public SensorsResource(String locId) {
        this.locId = locId;
    }

    /*
     * return the specific lcdId's Sensors
     * 
     * success - return 200 (Ok) and JSON representation Sensor
     * success - return 204 (No Content) if locId has no Sensors
     * failure - return 404 (Not Found) if locId is invalid     
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLocationSensors() {
        if (LocationDao.instance.getLocationById(locId) == null) {
            return Response.status(404).build();
        }
        Collection<Sensor> sensors = SensorDao.instance.getAllLocationSensors(locId);
        if (sensors.isEmpty()) {
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        return Response.ok((Sensor[]) sensors.toArray(new Sensor[sensors.size()])).build();
    }

    /*
     * adds sensor to database contained in locId
     *
     * returns instance of added Sensor
     *
     * success - returns 201 (Created) with URI of new Sensor and Sensor JSON in the body
     * failure - returns 400 (Bad Request) if the Sensor could not be added
     *           returns 500 (Server Error) if the Sensor could was added and 
     *                                      URI building error occurred
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postLocationSensor(Sensor in) {
        Sensor s = SensorDao.instance.addLocationSensor(locId, in);
        if (s == null) {
            return Response.status(400).build();
        } else {
            try {
                URI createdUri = new URI("locations/" + locId + "/sensors/" + s.getSensorId());
                return Response.created(createdUri).entity(s).build();
            } catch (URISyntaxException ex) {
                Logger.getLogger(SensorsResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.serverError().build();
            }
        }
    }

    // ---- BEGIN sub-resource locators ----
    
    /*
     * return SensorResource handler for specified sensorid
     */
    @Path("{sensorid: [0-9]+}")
    public SensorResource getSensorResource(@PathParam("sensorid") String sensorId) {
        return new SensorResource(locId, sensorId);
    }
}
