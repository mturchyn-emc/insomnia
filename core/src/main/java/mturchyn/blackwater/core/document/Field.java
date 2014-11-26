package mturchyn.blackwater.core.document;

import java.io.Serializable;

public class Field implements Serializable {

    private String name;
    private Object value;

    public Field() {
    }

    public static Field from(String name, Object value) {
        Field field = new Field();
        field.name = name;
        field.value = value;
        return field;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
