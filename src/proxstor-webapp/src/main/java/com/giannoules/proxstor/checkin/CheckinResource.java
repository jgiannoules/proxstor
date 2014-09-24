package com.giannoules.proxstor.checkin;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/checkin")
public class CheckinResource {
    
    @Path("device/{devid}")
    public CheckinDeviceResource getCheckinDeciceResource(@PathParam("devid") String devId) {
        return new CheckinDeviceResource(devId);
    }
    
    @Path("user/{userid}")
    public CheckinUserResource getCheckinUserResource(@PathParam("userid") String userId) {
        return new CheckinUserResource(userId);
    }    
   
}
