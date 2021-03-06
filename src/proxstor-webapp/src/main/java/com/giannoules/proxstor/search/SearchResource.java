package com.giannoules.proxstor.search;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.location.LocationDao;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.environmental.EnvironmentalDao;
import com.giannoules.proxstor.api.User;
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
        Collection<User> users = UserDao.instance.getMatching(u);  
        if (users == null || users.isEmpty()) {
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
    public Response getMatchingDevices(Device d) {
        Collection<Device> devices = DeviceDao.instance.getMatching(d);      
        if (devices == null || devices.isEmpty()) {
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
        Collection<Location> locations = LocationDao.instance.getMatching(l);
        if (locations == null || locations.isEmpty()) {
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        return Response.ok((Location[]) locations.toArray(new Location[locations.size()])).build();
    }
    
    /*
     * returns all environmentals matching criteria in partially expressed User JSON
     */
    @Path("environmentals")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatchingEnvironmentals(Environmental s) {
        Collection<Environmental> environmentals = EnvironmentalDao.instance.getMatching(s);
        if (environmentals == null || environmentals.isEmpty()) {
            return Response.noContent().build();
        }
        /*
         * ok() will not take Collection directly, so convert to array
         */
        return Response.ok((Environmental[]) environmentals.toArray(new Environmental[environmentals.size()])).build();
    }

}