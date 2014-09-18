package com.giannoules.proxstor.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
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
