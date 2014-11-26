package mturchyn.blackwater.core.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Document implements Serializable {

    private long id;
    List<Field> fields = new ArrayList<>(0);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
