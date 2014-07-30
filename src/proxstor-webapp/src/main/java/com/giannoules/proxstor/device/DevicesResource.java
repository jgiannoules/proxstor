/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giannoules.proxstor.device;

import com.giannoules.proxstor.user.UserDao;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author James_Giannoules
 */
@Path("/users/{userid}/devices")
public class DevicesResource {

    private boolean validUserId;
    private String userId;

    public DevicesResource(@PathParam("userid") String userId) {
        this.userId = userId;
        this.validUserId = UserDao.instance.validUserId(userId);
    }

    @Path("{devid}")
    public DeviceResource getDeviceResource(
            @PathParam("devid") String devId) {
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
            return DeviceDao.instance.addDevice(userId, dev);
        }
        return null;
    }
}
