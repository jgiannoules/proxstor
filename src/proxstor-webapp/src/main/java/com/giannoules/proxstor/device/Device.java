package com.giannoules.proxstor.device;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Device {
    private String devId;
    private String description;

    public Device() {}
    
    public Device(String description) {
        this.description = description;
    }
    
    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
