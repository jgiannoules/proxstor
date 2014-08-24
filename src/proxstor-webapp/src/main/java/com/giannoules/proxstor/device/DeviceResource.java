package com.giannoules.proxstor.device;

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
    public Response putDeviceUserIdDevId(Device dev) {
        if ((dev.getDevId() != null) && devId.equals(dev.getDevId())) {
            if (DeviceDao.instance.updateUserDevice(userId, dev)) {
                return Response.noContent().build();
            }
        }
        return Response.status(404).build();
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
        Device d = DeviceDao.instance.getUserDevice(userId, devId);
        if (d == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(d).build();
    }

    /*
     * remove userId's Device devId from Graph
     *
     * success - return 204 (No Content)
     * failure - return 404 (Not Found)     
     */
    @DELETE
    public Response deleteUserDevice() {
        if (DeviceDao.instance.deleteUserDevice(userId, devId)) {
            return Response.noContent().build();
        } else {
            return Response.status(404).build();
        }
    }

}
