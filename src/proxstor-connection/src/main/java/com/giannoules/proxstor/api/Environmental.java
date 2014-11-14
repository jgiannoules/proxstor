package com.giannoules.proxstor.api;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * ProxStor representation of an environmental. An environmental is the component
 * within a location which is sensed by the mobile device sensor. ProxStor defines
 * some basic fields here, but primary importance is upon the type and identifier.
 * 
 * This class is augmented with annotations
 * identifying it for Java Architecture for XML Binding.
 * 
 * @author James Giannoules
 */
@XmlRootElement
public class Environmental {

    /**
     * The description of the environmental. Opaque to ProxStor.
     */
    public String description;

    /**
     * The type of this Environmental. See @EnvironmentalType
     */
    public EnvironmentalType type;

    /**
     * The unique identifier for this environmental type. The uniqueness is
     * critical to ProxStor as this is the property allowing check-ins based
     * only the Environmental type & identifier.
     */
    public String identifier;

    /**
     * Globally unique database ID for this object (vertex)
     */
    public String environmentalId;
 
    /**
     * Default do-nothing constructor
     */
    public Environmental() {}
    
    /**
     * Get description of this environmental
     * @return description of elemental
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the elemental description
     * @param description new description for elemental
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * get the unique identifier for the elemental
     * @return elemental unique identifier
     */
    public String getEnvironmentalId() {
        return environmentalId;
    }
    
    /**
     * set the unique identifier for the elemental.
     * 
     * used internally by ProxStor.
     * clients may use this for their own tracking purposes.
     * @param environmentalId new identifier for elemental
     */
    public void setEnvironmentalId(String environmentalId) {
        this.environmentalId = environmentalId;
    }
    
    /**
     * get the type for this elemental.
     * See @EnvironmentalType 
     * @return elemental type
     */
    public EnvironmentalType getType() {
        return type;
    }
    
    /**
     * set the environmental type 
     * @param type new environmental type
     */
    public void setType(EnvironmentalType type) {
        this.type = type;
    }

    /**
     * get the identifier for this environmental
     * @return identifier 
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * set the unique identifier for this elemental
     * @param identifier identifier
     */
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
