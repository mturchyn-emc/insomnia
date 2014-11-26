package mturchyn.blackwater.file.keyvalue;

import java.io.Serializable;

class KeyEntry<T> implements Serializable {

    private T key;
    private long pos;
    private int size;

    KeyEntry(T key, long pos, int size) {
        this.key = key;
        this.pos = pos;
        this.size = size;
    }

    KeyEntry() {
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
