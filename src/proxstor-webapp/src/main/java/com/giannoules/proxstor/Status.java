package com.giannoules.proxstor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Status {
    public boolean ok;
    public String message;
    public String url;

    public Status() {}
    
    public Status(boolean ok, String message, String url) {
        this.ok = ok;
        this.message = message;
        this.url = url;
    }
    
    /**
     * @return the ok
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * @param ok the ok to set
     */
    public void setOk(boolean ok) {
        this.ok = ok;
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
}
