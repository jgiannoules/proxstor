package com.giannoules.proxstor.device;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * JAXB representation of a ProxStor device
 *      @TODO move out to separate package/jar for client use as well
 *
 */

@XmlRootElement
public class Device {
    private String devId;
    public String manufacturer;
    public String model;
    public String os;
    private String description;
    private String serialNum;

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
    
    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }
    
    @Override
    public String toString() {
        return devId + ": " + description + " (" + manufacturer + " " + model + " " + os + ")";
     }
    
    @Override
    public int hashCode() {
        return serialNum.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Device other = (Device) obj;
        if (!Objects.equals(this.devId, other.devId)) {
            return false;
        }       
        return true;
    }

}
