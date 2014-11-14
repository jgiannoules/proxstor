package com.giannoules.proxstor.api;

import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ProxStor representation of an locality. An locality is the vertex created
 * whenever a user "checks-in" to a location. The association is tracked in this
 * object, which captures:
 *  - user
 *  - location
 *  - device (if not manual mode)
 *  - environmental sensed (if not manual mode)
 *  - whether the check in was manual
 *  - whether the user is actively in the location
 *  - date/time of arrival
 *  - date/time of departure
 * 
 * This class is augmented with annotations
 * identifying it for Java Architecture for XML Binding.
 * 
 * @author James Giannoules
 */
@XmlRootElement
public class Locality {
    
    /**
     * database unique identifier for this Locality
     */
    String localityId;             // unique ID of locality
    
    /**
     * id of user for this locality
     */
    public String userId;          // associated userId

    /**
     * id of location for this locality
     */
    public String locationId;      // associated locationId (manual & auto)

    /**
     * id of device for this locality
     */
    public String deviceId;        // device associated with checkin

    /**
     * environmental id sensed for this locality
     */
    public String environmentalId; // environmental associated with checkin

    /**
     * set to true if check-in mode is manual
     */
    public boolean manual;         // manual entry?

    /**
     * set to true if locality is active (the user is still in the location)
     */
    public boolean active;         // is locality currently active?

    /**
     * date/time of arrival
     */
    public Date arrival;       // arrival time

    /**
     * date/time of departure
     */
    public Date departure;     // departure time
    
    /**
     * basic empty constructor
     */
    public Locality() {
    }
    
    /**
     * setter for locality id
     * @param id locality new id
     */
    public void setLocalityId(String id) {
        localityId = id;
    }
    
    /**
     * getter for user id
     * @return user id associated with locality
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * setter for user id
     * @param userId user id for this locality
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * getter for locality id
     * @return id for this locality
     */
    public String getLocalityId() {
        return localityId;
    }
    
    /**
     * getter for device id associated with this locality
     * @return device id; null if mode was manual
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * setter for device id
     * @param deviceId device id for this locality
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * getter for manual
     * @return true if manual mode; false otherwise
     */
    public boolean isManual() {
        return manual;
    }

    /**
     * setter for manual mode
     * @param manual true if check-in was manual; false otherwise
     */
    public void setManual(boolean manual) {
        this.manual = manual;
    }

    /**
     * getter for active
     * @return true if locality is active; false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * setter for active
     * @param active representation of active state of locality
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * setter for environmental id
     * @param environmentalId environmental id associated with locality
     */
    public void setEnvironmentalId(String environmentalId) {
        this.environmentalId = environmentalId;
    }
        
    /**
     * getter for environmental id
     * @return
     */
    public String getEnvironmentalId() {
        return environmentalId;
    }
    
    /**
     * getter for location id
     * @return
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * setter for location id
     * @param locationId
     */
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    /**
     * getter for arrival Date
     * @return Date of arrival
     */
    public Date getArrival() {
        return arrival;
    }

    /**
     * setter for arrival Date
     * @param arrive Date of arrival
     */
    public void setArrival(Date arrive) {
        this.arrival = arrive;
    }

    /**
     * getter for departure Date
     * @return Date of departure
     */
    public Date getDeparture() {
        return departure;
    }

    /**
     * setter for departure Date
     * @param departure Date of departure
     */
    public void setDeparture(Date departure) {
        this.departure = departure;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Locality [").append(localityId).append("]:\n");
        sb.append("\tmanual: ").append(manual).append("\n");
        sb.append("\tuserId: ").append(userId).append("\n");
        sb.append("\tdevId: ").append(deviceId).append("\n");        
        sb.append("\tenvironmentalId: ").append(environmentalId).append("\n");
        sb.append("\tlocId: ").append(locationId).append("\n");
        sb.append("\tarrive: ").append(arrival).append("\n");
        sb.append("\tdepart: ").append(departure);
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
        if (userId != null) {
            hashCode *= userId.hashCode();
        }
        if (environmentalId != null) {
            hashCode *= environmentalId.hashCode();
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
        if (!Objects.equals(this.userId, other.userId)) {
            return false;
        }
        if (!Objects.equals(this.environmentalId, other.environmentalId)) {
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
