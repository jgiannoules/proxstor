package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * ProxStor representation of an environmental type. The environmental type
 * captures the type of sensor/environmental. The different types represent the
 * sensor used. ProxStor does search for matches based on type, however ProxStor
 * does not specifically limit itself to certain types. Extending this enumeration
 * to include new types is possible without changes to the underlying storage
 * model.
 * 
 * This class is augmented with annotations
 * identifying it for Java Architecture for XML Binding.
 * 
 * @author James Giannoules
 */
@XmlRootElement
/**
 * currently there are six defined types:
 * NONE - representing the default condition
 * UNKNOWN - catch-all for yet-to-be defined or unknown
 * WIFI_SSID - representing WiFi SSID
 * WIFI_BSSID - representing WiFi BSSID
 * BT_UUID - representing bluetooth UUID
 * BLE_UUID - representing bluetooth low energy UUID
 */
public enum EnvironmentalType {
    NONE,
    UNKNOWN,
    WIFI_SSID,
    WIFI_BSSID,
    BT_UUID,
    BLE_UUID,    
};
