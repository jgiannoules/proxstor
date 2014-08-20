package com.giannoules.proxstor.user;

import com.tinkerpop.blueprints.Vertex;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * JAXB representation of a ProxStor user
 *      @TODO move out to separate package/jar for client use as well
 *
 * 2014/08/19 - updated to accept com.tinkerpop.blueprints.Vertex in constructor
 */


@XmlRootElement
public class User {
    public String userId;
    public String firstName;
    public String lastName;
    public String address;
    
    public User() {}
    
    public User(Vertex v) {
         firstName = v.getProperty("firstName");
         lastName = v.getProperty("lastName");
         address = v.getProperty("address");
         Object id = v.getId();
         if (id instanceof Long) {            
            userId = Long.toString((Long) v.getId());
         } else {
             userId = v.getId().toString();
         }
        
    }
    
    public User(String firstName, String lastName, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
}
