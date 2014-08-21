package com.giannoules.proxstor.device;

import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/devices")
public class DevicesResource {

    
    /*
     * returns all devices system-wide!
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Device> getAllDevices() {    
        return DeviceDao.instance.getAllDevices();
    }
    
    /*
     * returns specified devid device, otherwise 404
     * @TODO fix me
     */
    @Path("{devid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Device getDevice(@PathParam("devid") String devId) {        
        return DeviceDao.instance.getDevice(devId);
    } 
}