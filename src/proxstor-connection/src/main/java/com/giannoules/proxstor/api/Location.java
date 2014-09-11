package com.giannoules.proxstor.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    public String address;
    public LocationType type;
    public long latitude;
    public long longitude;
    /*
     * contains sensorId of unique sensors found in this location
     */
    public Set<String> sensors;
    public Set<String> within;
    public List<String> nearbyLocId;
    public List<Integer> nearbyDistance;
    
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public void addSensor(Sensor s) {
        if (sensors == null) {
            sensors = new HashSet<>();
        }
        sensors.add(s.getSensorId());
    }

    public boolean hasSensor(Sensor s) {
        if (sensors == null) {
            return false;
        }
        return (sensors.contains(s.getSensorId()));
    }

    public Set<String> getWithin() {
        return within;
    }

    public void setWithin(Set<String> within) {        
        this.within = within;
    }
    
    public void addWithin(String l) {
        if (within == null) {
            within = new HashSet<>();
        }
        this.within.add(l);
    }

    public boolean isWithin(String l) {
        if (within != null) {
            return this.within.contains(l);
        }
        return false;
    }
    
    public void addNearby(String locId, int d) {
        if ((nearbyLocId == null) || (nearbyDistance == null)) {
            nearbyLocId = new ArrayList<>();
            nearbyDistance = new ArrayList<>();
        }
        nearbyLocId.add(locId);
        nearbyDistance.add(d);
    }

    @Override
    public String toString() {
        return locId + ": " + type + " - " + description + ", " + address + " [" + latitude + "/" + longitude + "]";
    }
    
    @Override
    public int hashCode() {
        return (int) (description.hashCode() * address.hashCode() * type.hashCode()
                * latitude * longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Location other = (Location) obj;
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.latitude, other.latitude)) {
            return false;
        }
        if (!Objects.equals(this.longitude, other.longitude)) {
            return false;
        }
        return true;
    }

}
