package mturchyn.blackwater.core.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.String;import java.util.List;

@XmlRootElement(name = "settings")
public class Settings {

    private String version;
    private List<Property> properties;

    @XmlAttribute
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}
