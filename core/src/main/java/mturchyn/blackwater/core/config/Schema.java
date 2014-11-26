package mturchyn.blackwater.core.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.String;import java.util.List;

@XmlRootElement(name = "schema")
public class Schema {

    private String version;
    private List<DocumentDescriptor> documents;

    @XmlAttribute(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElementWrapper(name = "documents")
    @XmlElement(name = "document")
    public List<DocumentDescriptor> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDescriptor> documents) {
        this.documents = documents;
    }
}
