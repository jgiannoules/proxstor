package com.giannoules.proxstor.checkin;

import javax.ws.rs.Path;

public class CheckinDeviceResource {

    private final String devId;

    public CheckinDeviceResource(String devId) {
        this.devId = devId;
    }
    
    @Path("environmental")
    public CheckinDeviceEnvironmentalResource getCheckinDeviceEnvironmentalResource()  {          
        return new CheckinDeviceEnvironmentalResource(devId);
    }
    
}
