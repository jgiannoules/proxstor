package com.giannoules.proxstor.device;

import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/devices")
public class DevicesResource {

    
    /*
    public DevicesResource(@PathParam("userid") String userId) {
        this.userId = userId;
        this.validUserId = UserDao.instance.validUserId(userId);
    }

    @Path("{devid}")
    public DeviceResource getDeviceResource(@PathParam("devid") String devId) {
        if (validUserId) {
            return new DeviceResource(userId, devId);
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Device> getDevices() {
        if (validUserId) {
            List<Device> devices = new ArrayList<Device>();
            devices.addAll(DeviceDao.instance.getAllDevices(userId));
            return devices;
        }
        return null;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Device postDevice(Device dev) {
        if (validUserId) {
            return DeviceDao.instance.addDevice(UserDao.instance.getUser(userId), dev);
        }
        return null;
    }
    */
    
    @GET
    public Collection<Device> getAllDevices() {
        return DeviceDao.instance.getAllDevices();
    }
    
    @Path("{devid}")
    public Device getDevice(@PathParam("devid") String devId) {
        return DeviceDao.instance.getDevice(devId);
    }
}
