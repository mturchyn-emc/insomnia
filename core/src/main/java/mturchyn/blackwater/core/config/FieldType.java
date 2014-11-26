package mturchyn.blackwater.core.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum FieldType {
    @XmlEnumValue("long") LONG,
    @XmlEnumValue("int") INT,
    @XmlEnumValue("text") TEXT,
    @XmlEnumValue("date") DATE,
    @XmlEnumValue("timestamp") TIMESTAMP
}
