package mturchyn.blackwater.file.keyvalue;

import java.io.IOException;

public interface KeyValueRepository<T, V> {

    V put(T key, V value) throws IOException;

    V read(T key) throws IOException;

    boolean contains(T key) throws IOException;

    V remove(T key) throws IOException;

    void clear() throws IOException;

    void close() throws IOException;

    void save() throws IOException;

}
