package com.giannoules.proxstor.user;

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

}
