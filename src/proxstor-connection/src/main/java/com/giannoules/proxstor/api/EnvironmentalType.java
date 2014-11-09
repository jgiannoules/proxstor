package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public enum EnvironmentalType {
    NONE,
    UNKNOWN,
    WIFI_SSID,
    WIFI_BSSID,
    BT_UUID,
    BLE_UUID,    
};
