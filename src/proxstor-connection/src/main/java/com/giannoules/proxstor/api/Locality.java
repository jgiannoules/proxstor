package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;

@XmlRootElement
public class Locality {
    
    String localityId;             // unique ID of locality
    
    public String locationId;      // associated locationId (manual & auto)
    public String deviceId;        // device associated with checkin
    public String sensorId;        // sensor associated with checkin

    public boolean manual;         // manual entry?
    public boolean active;         // is locality currently active?

    public DateTime arrival;       // arrival time
    public DateTime departure;     // departure time

    
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

    public DateTime getArrival() {
        return arrival;
    }

    public void setArrival(DateTime arrive) {
        this.arrival = arrive;
    }

    public DateTime getDeparture() {
        return departure;
    }

    public void setDeparture(DateTime departure) {
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
    
}
