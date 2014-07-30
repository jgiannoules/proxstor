/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.giannoules.proxstor.device;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author James_Giannoules
 */
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
