package com.giannoules.proxstor.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * ProxStor representation of a user. A user is the component representing each
 * unique human (user) of the system. ProxStor currently defines some basic 
 * fields in here, but primary importance is the email address.
 * 
 * This class is augmented with annotations
 * identifying it for Java Architecture for XML Binding.
 * 
 * @author James Giannoules
 */
@XmlRootElement
public class User {

    /**
     * database unique identifier for user
     */
    public String userId;
    /**
     * user's first name
     */
    public String firstName;
    /**
     * user's last name
     */
    public String lastName;
    /**
     * user's email address
     */
    public String email;
    /**
     * contains devIDs of unique devices used by this user
     */
    public Set<String> devices;
    /**
     * track userId of known users and the getKnowsStrength relationship strength
     */
    public List<String> knows;
    /**
     * strength of knows relationships to users in knows list
     */
    public List<Integer> strength;

    public User() {
    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void addDevice(Device d) {
        if (devices == null) {
            devices = new HashSet<>();
        }
        devices.add(d.getDevId());
    }
    
    public boolean hasDevice(Device d) {
        if (devices == null) {
            return false;
        }
        return (devices.contains(d.getDevId()));
    }
    
    @Override
    public String toString() {
        return userId + ": " + lastName + ", " + firstName + " [" + email + "]";
    }
    
    @Override
    public int hashCode() {
        return firstName.hashCode() * lastName.hashCode() * email.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (!Objects.equals(this.firstName, other.firstName)) {
            return false;
        }
        if (!Objects.equals(this.lastName, other.lastName)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        return true;
    }

    public List<String> getKnows() {
        return knows;
    }

    public void setKnows(List<String> knows) {
        this.knows = knows;
    }
    
    public void addKnows(String id, int strength) {
        if ((this.knows == null) || (this.strength == null)) {
            this.knows = new ArrayList<>();
            this.strength = new ArrayList<>();
        }
        this.knows.add(id);
        this.strength.add(strength);
    }
    
    public Integer getKnowsStrength(String id) {
        if (this.knows != null && this.knows.contains(id)) {
            return this.strength.get(this.knows.indexOf(id));
        }
        return null;
    }
        
    public List<Integer> getStrength() {
        return strength;
    }

    public void setStrength(List<Integer> strength) {
        this.strength = strength;
    }

}
