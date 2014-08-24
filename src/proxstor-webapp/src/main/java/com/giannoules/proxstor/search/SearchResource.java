package com.giannoules.proxstor.search;

import com.giannoules.proxstor.device.Device;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.location.Location;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.sensor.Sensor;
import com.giannoules.proxstor.sensor.SensorDao;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("search")
public class SearchResource {

    /*
     * returns all users matching criteria in partially expressed User JSON
     */
    @Path("users")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatchingUsers(User u) {
        Collection<User> users = UserDao.instance.getMatchingUsers(u);
        if (users == null) {
            return Response.status(404).build();
        }
        if (users.isEmpty()) {
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        return Response.ok((User[]) users.toArray(new User[users.size()])).build();
    }

    /*
     * returns all devices matching criteria in partially expressed User JSON
     */
    @Path("devices")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMathingDevices(Device d) {
        Collection<Device> devices = DeviceDao.instance.getMatchingDevices(d);
        if (devices == null) {
            return Response.status(404).build();
        }
        if (devices.isEmpty()) {
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        return Response.ok((Device[]) devices.toArray(new Device[devices.size()])).build();
    }

    /*
     * returns all locations matching criteria in partially expressed User JSON
     */
    @Path("locations")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatchingLocations(Location l) {
        Collection<Location> locations = LocationDao.instance.getMatchingLocations(l);
        if (locations == null) {
            return Response.status(404).build();
        }
        if (locations.isEmpty()) {
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        return Response.ok((Location[]) locations.toArray(new Location[locations.size()])).build();
    }
    
    /*
     * returns all sensors matching criteria in partially expressed User JSON
     */
    @Path("sensors")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatchingSensors(Sensor s) {
        Collection<Sensor> sensors = SensorDao.instance.getMatchingSensors(s);
        if (sensors == null) {
            return Response.status(404).build();
        }
        if (sensors.isEmpty()) {
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        return Response.ok((Sensor[]) sensors.toArray(new Sensor[sensors.size()])).build();
    }

}