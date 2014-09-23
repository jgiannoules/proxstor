package com.giannoules.proxstor.api;

import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Locality {
    
    String localityId;             // unique ID of locality
    
    public String locationId;      // associated locationId (manual & auto)
    public String deviceId;        // device associated with checkin
    public String sensorId;        // sensor associated with checkin

    public boolean manual;         // manual entry?
    public boolean active;         // is locality currently active?

//    public DateTime arrival;       // arrival time
//    public DateTime departure;     // departure time
    public Date arrival;       // arrival time
    public Date departure;     // departure time

    
    public Locality() {
    }
    
    public void setLocalityId(String id) {
        localityId = id;
    }
    
    public String getLocalityId() {
        return localityId;
    }
    
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
    
    public String getSensorId() {
        return sensorId;
    }
    
    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Date getArrival() {
        return arrival;
    }

    public void setArrival(Date arrive) {
        this.arrival = arrive;
    }

    public Date getDeparture() {
        return departure;
    }

    public void setDeparture(Date departure) {
        this.departure = departure;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(localityId).append(": ");
        sb.append("devId: ").append(deviceId).append(" ");
        sb.append("manual: ").append(manual).append(" ");
        sb.append("sensorId: ").append(sensorId).append(" ");
        sb.append("arrive: ").append(arrival).append(" ");
        sb.append("depart: ").append(departure);
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        int hashCode = locationId.hashCode();
        if (arrival != null) {
            hashCode *= arrival.hashCode();
        }
        if (departure != null) {
            hashCode *= departure.hashCode();
        }
        if (deviceId != null) {
            hashCode *= deviceId.hashCode();
        }
        if (sensorId != null) {
            hashCode *= sensorId.hashCode();
        }
        if (active) {
            hashCode *= 5;
        }
        if (manual) {
            hashCode *= 7;
        }
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
         if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Locality other = (Locality) obj;
        if (!Objects.equals(this.arrival, other.arrival)) {
            return false;
        }
        if (!Objects.equals(this.departure, other.departure)) {
            return false;
        }
        if (!Objects.equals(this.deviceId, other.deviceId)) {
            return false;
        }
        if (!Objects.equals(this.sensorId, other.sensorId)) {
            return false;
        }
        if (!Objects.equals(this.locationId, other.locationId)) {
            return false;
        }
        if (!Objects.equals(this.active, other.active)) {
            return false;
        }
        if (!Objects.equals(this.manual, other.manual)) {
            return false;
        }
        return true;
    }
    
}
