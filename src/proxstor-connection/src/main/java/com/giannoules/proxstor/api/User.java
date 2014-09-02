package com.giannoules.proxstor.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * JAXB representation of a ProxStor user
 *      @TODO move out to separate package/jar for client use as well
 */

@XmlRootElement
public class User {

    public String userId;
    public String firstName;
    public String lastName;
    public String email;
    /*
     * contains devIDs of unique devices used by this user
     */
    public Set<String> devices;

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

}
