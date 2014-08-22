package com.giannoules.proxstor.location;

import com.giannoules.proxstor.device.DeviceResource;
import com.giannoules.proxstor.sensor.SensorResource;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

public class LocationResource {

    private final String locId;
    
    public LocationResource(String locId) {
        this.locId = locId;
    }
    
    /*
     * return SensorResource handler for this Location
     */
    @Path("sensors")
    public SensorResource getSensorResource() {
        return new SensorResource(locId);
    } 
    
   /*
     * return the specified locId Location
     * if the locId is invalid throw 404
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Location getLocation() {
        Location l = LocationDao.instance.getLocationById(locId);
        if (l == null){             
            throw new WebApplicationException(404);
        }
        return l;
    }
    
    /*
     * update location
     * locIdpath must match locId inside JSON (and be valid ID)
     * if not, return false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)    
    public boolean putLocation(Location l) {
        if ((l.getLocId() != null) && l.getLocId().equals(locId)) {
            return LocationDao.instance.updateLocation(l);            
        }
        return false;        
    }

    /*
     * remove locId from database
     *
     * if locId is not valid user return false
     */
    @DELETE    
    public boolean deleteLocation() {
        return LocationDao.instance.deleteLocation(locId);
    }
}
