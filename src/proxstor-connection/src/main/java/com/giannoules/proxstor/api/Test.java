package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Test {
    
    public Integer numUsers;
    public Integer numLocations;

    public Integer getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(Integer numUsers) {
        this.numUsers = numUsers;
    }

    public Integer getNumLocations() {
        return numLocations;
    }

    public void setNumLocations(Integer numLocations) {
        this.numLocations = numLocations;
    }
    
}
