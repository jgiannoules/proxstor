package com.giannoules.proxstor.device;

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

public class DeviceResource {

    private final String userId;

    /*
     * all these Paths assume userId context, so stash in constructor
     */
    public DeviceResource(String userId) {
        this.userId = userId;        
    }

    /*
     * all of userId's devices
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Device> getAllDevices() {
        return DeviceDao.instance.getAllUserDevices(userId);
    }
    
    /*
     * userId adding new device
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)    
    public Device postDevice(Device d) {
        return DeviceDao.instance.addUserDevice(userId, d);
    }

    /*
     * userId updating device
     * return 304 if
     *   - device not found
     *   - user doesn't own device
     *   - Device devId doesn't match devid @PathParam
     */
    @Path("{devid: [0-9]*}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)    
    public Response putDeviceDevId(@PathParam("devid") String devId, Device dev) {
        if ((dev.getDevId() != null) && devId.equals(dev.getDevId()) 
                && DeviceDao.instance.updateUserDevice(userId, dev)) {
            return Response.ok().build();
        }
        return Response.notModified().build();
    }
    
    /*
     * retrieve userId's device with devId
     * return 404 if not found
     */
    @Path("{devid: [0-9]*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("devid") String devId) {
        Device d = DeviceDao.instance.getUserDevice(userId, devId);
        if (d == null) {
            throw new WebApplicationException(404);
        }
        return Response.ok(d).build(); 
    }
    
    /*
     * remove userId's Device devId from Graph
     */
    @Path("{devid: [0-9]*}")
    @DELETE
    public boolean deleteDevice(@PathParam("devid") String devId) {
        return DeviceDao.instance.deleteUserDevice(userId, devId);
    }
    
}
