package com.giannoules.proxstor.api;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ProxStor representation of a device. A device is the mobile computing
 * entity which is reporting environmental encounters to the ProxStor web
 * service. Examples of devices include mobile phones, tablets, or embedded
 * smart sensors in your shoe.
 * 
 * This class is augmented with annotations
 * identifying it for Java Architecture for XML Binding.
 * 
 * @author James Giannoules
 */
@XmlRootElement
public class Device {
    
    /**
     * the database unique identifier for this device
     */
    private String devId;
    /**
     * the manufacturer of this device. used for searching and presentation
     * friendliness. opaque to ProxStor
     */
    public String manufacturer;
    /**
     * the model of this device. used for searching and presentation
     * friendliness. opaque to ProxStor
     */
    public String model;
    /**
     * the operating system of this device. used for searching and presentation
     * friendliness. opaque to ProxStor
     */
    public String os;
    /**
     * the description of this device. used for searching and presentation
     * friendliness. opaque to ProxStor
     */
    private String description;
    /**
     * the manufacturer unique serial number associated with this device.
     * intended as the mechanism to re-find a device when application changes
     * occur.
     */
    private String serialNum;

    /**
     * default constructor. no operation.
     */
    public Device() {
    }
    
    /**
     * set Device description
     * @param description New description
     */
    public Device(String description) {
        this.description = description;
    }
    
    /**
     * Retrieve the Device's device id. Only Device objects returned from 
     * ProxStor will have this value set. When adding new Devices the device
     * id will not be populated.
     * 
     * Device ID are globally unique for an entire ProxStor instance.
     * 
     * @return Device ID value.
     */
    public String getDevId() {
        return devId;
    }

    /**
     * Set the Device's device id. This is primarily used by ProxStor as Device
     * objects are being de-serialized from JSON as well as when they are being
     * retrieved and assembled from the database. A client cannot influence the
     * device id value with this method.
     * 
     * @param devId New device ID value.
     */
    public void setDevId(String devId) {
        this.devId = devId;
    }

    /**
     * Retrieve the device description.
     * @return Description String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the device description. This is intended to be human friendly text
     * describing the device in such a manner as to be meaningful to the user.
     * For example : "John Smith's Google Nexus 5"
     * 
     * @param description New description for device.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Retrieve device serial number. The serial number uniquely identifies a
     * certain class of devices. This is opaque data to ProxStor.
     * 
     * @return Device serial number.
     */
    public String getSerialNum() {
        return serialNum;
    }

    /**
     * Set the device serial number. This is intended to allow applications and
     * users a reference back to a specific device. The use makes the most sense
     * if the serial number is guaranteed globally unique by the manufacturer.
     * 
     * @param serialNum New serial number for the device.
     */
    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    /**
     * Retrieve the manufacturer of the device.
     * 
     * @return Device manufacturer.
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Set the device manufacturer. This is data stored in the event the
     * application can make use of it. For example, searches can be performed 
     * in ProxStor for all the devices with manufacturer XYZ.
     * 
     * This data is opaque to ProxStor.
     * 
     * @param manufacturer New manufacturer information.
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Retrieve the model of the device.
     * 
     * @return Device model.
     */
    public String getModel() {
        return model;
    }

    /**
     * Set the device model information. This is data stored in the event the
     * application can make use of it. For example, searches can be performed 
     * in ProxStor for all the devices of model XYZ.
     * 
     * This data is opaque to ProxStor.
     * 
     * @param model New device model information.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Retrieve the device operating system.
     * 
     * @return Device operating system. 
     */
    public String getOs() {
        return os;
    }

    /**
     * Set the device operating system. This is data stored in the event the
     * application can make use of it. For example, searches can be performed 
     * in ProxStor for all the devices running operating system XYZ.
     * 
     * This data is opaque to ProxStor.
     * 
     * @param os New operating system value.
     */
    public void setOs(String os) {
        this.os = os;
    }
    
    @Override
    public String toString() {
        return devId + ": " + description + " (" + manufacturer + " " + model + " " + os + ")";
     }
    
    @Override
    public int hashCode() {
        return serialNum.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Device other = (Device) obj;
        if (!Objects.equals(this.devId, other.devId)) {
            return false;
        }       
        return true;
    }

}
