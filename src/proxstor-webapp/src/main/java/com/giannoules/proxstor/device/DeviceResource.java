package com.giannoules.proxstor.device;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class DeviceResource {

    private String userId;
    private String devId;

    public DeviceResource(String userId, String devId) {
        this.userId = userId;
        this.devId = devId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Device getDevice() {
        return DeviceDao.instance.getDevice(userId, devId);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean putDevice(Device dev) {
        if (dev.getDevId() != null && devId.equals(dev.getDevId())) {
            return DeviceDao.instance.updateDevice(dev);
        }
        return false;
    }

    @DELETE
    public boolean deleteDevice() {
        return DeviceDao.instance.deleteDevice(userId);
    }

}
