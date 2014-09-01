package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * JAXB representation of a ProxStor Location
 *      @TODO move out to separate package/jar for client use as well
 *
 */

@XmlRootElement
public class Location {
    public String locId;
    public String description;
    
    public Location() {}
    
    public String getLocId() {
        return this.locId;
    }
    
    public void setLocId(String locId) {
        this.locId = locId;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
