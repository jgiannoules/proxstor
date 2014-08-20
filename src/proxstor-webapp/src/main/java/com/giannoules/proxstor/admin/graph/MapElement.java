
package com.giannoules.proxstor.admin.graph;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * see http://stackoverflow.com/questions/8413608/sending-list-map-as-post-parameter-jersey
 */
public class MapElement {
    @XmlElement
    public String key;
    @XmlElement
    public String value;

    private MapElement() {
    }

    public MapElement(String key, String value) {
        this.key = key;
        this.value = value;
    }
}