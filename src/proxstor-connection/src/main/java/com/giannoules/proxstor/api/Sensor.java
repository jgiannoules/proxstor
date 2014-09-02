package com.giannoules.proxstor.api;

import java.util.Objects;
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
    public String typeIdentifier;
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

    public String getTypeIdentifier() {
        return typeIdentifier;
    }

    public void setTypeIdentifier(String typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }
    
    @Override
    public String toString() {
        return sensorId + ": " + type.toString() + " - " + typeIdentifier + "(" + description + ")";
    }
    
    @Override
    public int hashCode() {
        return (type.hashCode() * typeIdentifier.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sensor other = (Sensor) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.typeIdentifier, other.typeIdentifier)) {
            return false;
        }
        return true;
    }
 
}
