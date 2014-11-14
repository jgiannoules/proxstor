package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * ProxStor representation of a location type. The location type
 * captures the type of location within ProxStor. The different types represent the
 * common different locations which might be stored.
 * 
 * This class is augmented with annotations
 * identifying it for Java Architecture for XML Binding.
 * 
 * @author James Giannoules
 */
@XmlRootElement
/**
 * currently there are eight defined types:
 * NONE - default 
 * UNKNOWN - type which doesn't fit into the below categories
 * POSTAL_CODE - a postal code (meant to contain other locations)
 * BUSINESS - a place of business
 * RESIDENCE - a residence
 * CITY - city
 * STATE - state containing city
 * COUNTRY - country containing states
 */
public enum LocationType {
    NONE,
    UNKNOWN,
    POSTAL_CODE,
    BUSINESS,
    RESIDENCE,
    CITY,
    STATE,
    COUNTRY,
}
