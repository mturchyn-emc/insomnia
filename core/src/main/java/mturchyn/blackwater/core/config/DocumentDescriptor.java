package mturchyn.blackwater.core.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.String;import java.util.List;

@XmlRootElement(name = "document")
public class DocumentDescriptor {

    private String name;
    private List<FieldDescriptor> fields;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    public List<FieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(List<FieldDescriptor> fields) {
        this.fields = fields;
    }
}
