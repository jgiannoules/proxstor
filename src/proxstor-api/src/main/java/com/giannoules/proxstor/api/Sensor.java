package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;


/*
 * JAXB representation of a ProxStor sensor
 *
 *      @TODO move out to separate package/jar for client use as well
 *
 */
@XmlRootElement
public class Sensor {
    public String description;
    public SensorType type;
    public String sensorId;
 
    public Sensor() {}
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSensorId() {
        return sensorId;
    }
    
    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
    
    public SensorType getType() {
        return type;
    }
    
    public void setType(SensorType type) {
        this.type = type;
    }
}
