package com.giannoules.proxstor.api;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;


/*
 * JAXB representation of a ProxStor environmental
 *
 *      @TODO move out to separate package/jar for client use as well
 *
 */
@XmlRootElement
public class Environmental {
    public String description;
    public EnvironmentalType type;
    public String identifier;
    public String environmentalId;
 
    public Environmental() {}
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEnvironmentalId() {
        return environmentalId;
    }
    
    public void setEnvironmentalId(String environmentalId) {
        this.environmentalId = environmentalId;
    }
    
    public EnvironmentalType getType() {
        return type;
    }
    
    public void setType(EnvironmentalType type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    @Override
    public String toString() {
        return environmentalId + ": " + type.toString() + " - " + identifier  + ")";
    }
    
    @Override
    public int hashCode() {
        return (type.hashCode() * identifier.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Environmental other = (Environmental) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.identifier, identifier)) {
            return false;
        }
        return true;
    }
 
}
