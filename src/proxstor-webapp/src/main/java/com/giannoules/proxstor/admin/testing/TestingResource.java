package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.ProxStorDebug;
import javax.ws.rs.Path;

public class TestingResource {
   
    @Path("/users")
    public TestingUserResource getTestingUserResource() {
        return new TestingUserResource();
    }
    
    @Path("/locations")
    public TestingLocationResource getTestingLocationResource() {
        return new TestingLocationResource();
    }
    
    @Path("/devices")
    public TestingDevicesResource getTestingDevicesResource() {
        return new TestingDevicesResource();
    }
    
    @Path("/environmentals")
    public TestingEnvironmentalsResource getTestingEnvironmentalsResource() {
        ProxStorDebug.println("getTestingEnvironmentalsResource");
        return new TestingEnvironmentalsResource();
    }
}
