package com.giannoules.proxstor.device;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * JAXB representation of a ProxStor device
 *      @TODO move out to separate package/jar for client use as well
 *
 */

@XmlRootElement
public class Device {
    private String id;
    private String description;

    public Device() {}
    
    public Device(String description) {
        this.description = description;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
