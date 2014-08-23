package com.giannoules.proxstor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Result {
    public Status status;           // overall result Status
    public String message;          // message to accompany status
    public Class dataClass;  // optional description of data to follow
    public Object data;             // generic "data" associated with result
    public String url;              // optional URL for user to reference
    
    public Result() {}
    
    public Result(Status status, String message, String url) {
        this.status = status;
        this.message = message;
        this.url = url;
    }
    
    /**
     * @return the status
     */
    public Status isStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data) {
        if (data != null) {
            this.data = data;
            this.dataClass = data.getClass();
        }
    }
   
}
