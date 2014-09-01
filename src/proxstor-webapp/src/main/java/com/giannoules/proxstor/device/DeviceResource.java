package com.giannoules.proxstor.device;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.exception.DeviceNotOwnedByUser;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidUserId;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DeviceResource {

    private final String userId;
    private final String devId;

    public DeviceResource(String userId, String devId) {
        this.userId = userId;
        this.devId = devId;
    }

    /*
     * update device devId belonging to User userId
     * Note: devId path must match devId inside JSON (and be valid ID)
     *
     * sucess - return 204 (No Content)
     * failure - return 404 (Not Found)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putDeviceUser(Device dev) {
        if ((dev.getDevId() == null) || !devId.equals(dev.getDevId())) {
            return Response.status(400).build();
        }
        try {
            if (DeviceDao.instance.update(userId, dev)) {
                return Response.noContent().build();
            } else {
                return Response.status(500).build();
            }
        } catch (InvalidUserId | InvalidDeviceId ex) {
            Logger.getLogger(DeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (DeviceNotOwnedByUser ex) {
            Logger.getLogger(DeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

    /*
     * return the specified userId User's devId Device
     * 
     * success - return 200 (Ok) and JSON representation Device
     * failure - return 404 (Not Found)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDevice() {
        Device d;
        try {
            d = DeviceDao.instance.getUserDevice(userId, devId);
            return Response.ok().entity(d).build();
        } catch (InvalidDeviceId | InvalidUserId ex) {
            Logger.getLogger(DeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (DeviceNotOwnedByUser ex) {
            Logger.getLogger(DeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

    /*
     * remove userId's Device devId from Graph
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)     
     */
    @DELETE
    public Response deleteUserDevice() {
        try {
            if (DeviceDao.instance.delete(userId, devId)) {
                return Response.noContent().build();
            } else {
                return Response.status(500).build();
            }
        } catch (InvalidUserId | InvalidDeviceId ex) {
            Logger.getLogger(DeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        } catch (DeviceNotOwnedByUser ex) {
            Logger.getLogger(DeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(400).build();
        }
    }

}
