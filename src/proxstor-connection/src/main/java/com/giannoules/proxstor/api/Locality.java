package com.giannoules.proxstor.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;

@XmlRootElement
public class Locality {
    
    String localityId;             // unique ID of locality
    
    public String locId;           // associated locId (manual & auto)
    public String devId;           // device associated with checkin
    public String sensorId;        // sensor associated with checkin

    public boolean manual;         // manual entry?
    public boolean active;         // is locality currently active?

//    List<String> sensors;          // list of sensors sensed in this locality

    public DateTime arrival;        // arrival time
    public DateTime departure;     // departure time

    
    public Locality() {
    }
    
    public Locality(Sensor s) {
//        sensors = new ArrayList<>();
//        sensors.add(s.getSensorId());
    }
    
    public void setLocalityId(String id) {
        localityId = id;
    }
    
    public String getLocalityId() {
        return localityId;
    }
    
//    public List<String> getSensors() {
//        return sensors;
//    }
//    
//    public void addSensor(Sensor s) {
//        if (sensors == null) {
//            sensors = new ArrayList<>();
//        }
//        if (!sensors.contains(s.getSensorId())) {
//            sensors.add(s.getSensorId());
//        }
//    }

    public String getDeviceId() {
        return devId;
    }

    public void setDeviceId(String deviceId) {
        this.devId = deviceId;
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

//    public SensorType getSensorType() {
//        return sensorType;
//    }
//
//    public void setSensorType(SensorType sensorType) {
//        this.sensorType = sensorType;
//    }
//
//    public String getSensorValue() {
//        return sensorValue;
//    }
//
//    public void setSensorValue(String sensorValue) {
//        this.sensorValue = sensorValue;
//    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
    
    public String getSensorId() {
        return sensorId;
    }
    
    public String getLocationId() {
        return locId;
    }

    public void setLocationId(String locationId) {
        this.locId = locationId;
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
        sb.append("devId: ").append(devId).append(" ");
        sb.append("manual: ").append(manual).append(" ");
//        sb.append("sensorType: ").append(sensorType).append(" ");
//        sb.append("sensorValue: ").append(sensorValue).append(" ");
        sb.append("sensorId: " + sensorId);
        sb.append("arrive: ").append(arrival).append(" ");
        sb.append("depart: ").append(departure);
        return sb.toString();
    }
    
}
