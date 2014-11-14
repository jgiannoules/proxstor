package com.giannoules.proxstor.api;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * ProxStor representation of an query. A query is the component issued to 
 * the service which requesting a query be performed in the database.
 *
 * This class is augmented with annotations
 * identifying it for Java Architecture for XML Binding.
 * 
 * @author James Giannoules
 */
@XmlRootElement
public class Query {
    public String userId;
    public String locationId;
    public Integer strength;
    public Double distance;    // in meters
    public Date dateStart;
    public Date dateEnd;    

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Query {\n");
        if (userId != null) {
            sb.append("\tuserId: ").append(userId).append("\n");
        }
        if (locationId != null) {
            sb.append("\tlocationId: ").append(locationId).append("\n");
        }
        if (strength != null) {
            sb.append("\tstrength: ").append(strength).append("\n");
        }
        if (distance != null) {
            sb.append("\tdistance: ").append(distance).append("\n");
        }
        if (dateStart != null) {
            sb.append("\tdateStart: ").append(dateStart).append("\n");
        }
        if (dateEnd != null) {
            sb.append("\tdateEnd: ").append(dateEnd).append("\n");
        }
        sb.append("}").append("\n");
        return sb.toString();
     }
    
    @Override
    public int hashCode() {
        int hash = 1;
        if (userId != null) {
            hash *= userId.hashCode();
        }
        if (locationId != null) {
            hash *= locationId.hashCode();
        }
        if (strength != null) {
            hash *= strength.hashCode();
        }
        if (distance != null) {
            hash *= distance.hashCode();
        }
        if (dateStart != null) {
            hash *= dateStart.hashCode();
        }
        if (dateEnd != null) {
            hash *= dateEnd.hashCode();
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Query other = (Query) obj;
        if (this.userId != null) {
            if ((other.getUserId() == null) || (!other.getUserId().equals(this.userId))) {
                return false;
            }
        }
        if (this.locationId != null) {
            if ((other.getLocationId()== null) || (!other.getLocationId().equals(this.locationId))) {
                return false;
            }
        }
        if (this.strength != null) {
            if ((other.getStrength()== null) || (!other.getStrength().equals(this.strength))) {
                return false;
            }
        }
        if (this.distance != null) {
            if ((other.getDistance()== null) || (!other.getDistance().equals(this.distance))) {
                return false;
            }
        }
        if (this.dateStart != null) {
            if ((other.getDateStart()== null) || (!other.getDateStart().equals(this.dateStart))) {
                return false;
            }
        }
        if (this.dateEnd != null) {
            if ((other.getDateEnd()== null) || (!other.getDateEnd().equals(this.dateEnd))) {
                return false;
            }
        }
        
        return true;
    }
}
