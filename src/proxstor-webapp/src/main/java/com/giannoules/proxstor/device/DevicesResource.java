package com.giannoules.proxstor.device;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.exception.InvalidUserId;
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

public class DevicesResource {

    private final String userId;

    /*
     * all these Paths assume userId context, so stash in constructor
     */
    public DevicesResource(String userId) {
        this.userId = userId;
    }

    /*
     * return the specified userId's Devices
     * 
     * success - return 200 (Ok) and JSON representation User
     * success - return 204 (No Content) if userId has no Devices
     * failure - return 404 (Not Found) if userId is invalid     
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUserDevices() {
        Collection<Device> devices;
        try {
            devices = DeviceDao.instance.getAllUserDevices(userId);
            if (devices.isEmpty()) {
                return Response.noContent().build();
            }
            /*
             * ok() will not take Collection directly, so convert to array
             */
            return Response.ok((Device[]) devices.toArray(new Device[devices.size()])).build();
        } catch (InvalidUserId ex) {
            Logger.getLogger(DevicesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    /*
     * adds device to database used by userId
     *
     * returns instance of added User
     *
     * success - returns 201 (Created) with URI of new Device and Device JSON in the body
     * failure - returns 400 (Bad Request) if the Device could not be added
     *           returns 404 (Not Found) if the UserId is invalid
     *           returns 500 (Server Error) if the Device could was added and 
     *                                      URI building error occurred
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postUserDevice(Device in) {
        Device d;
        try {
            d = DeviceDao.instance.add(userId, in);
            if (d == null) {
                return Response.status(400).build();
            } else {
                try {
                    URI createdUri = new URI("users/" + userId + "/devices/" + d.getDevId());
                    return Response.created(createdUri).entity(d).build();
                } catch (URISyntaxException ex) {
                    Logger.getLogger(DevicesResource.class.getName()).log(Level.SEVERE, null, ex);
                    return Response.serverError().build();
                }
            }
        } catch (InvalidUserId ex) {
            Logger.getLogger(DevicesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    // ---- BEGIN sub-resource locators ----
    /*
     * return DeviceResource handler for specified devid
     */
    @Path("{devid: [0-9]+}")
    public DeviceResource getDeviceResource(@PathParam("devid") String devId) {
        return new DeviceResource(userId, devId);
    }

}
