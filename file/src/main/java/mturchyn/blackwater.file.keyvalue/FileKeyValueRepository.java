package mturchyn.blackwater.file.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class FileKeyValueRepository<T, V> implements KeyValueRepository<T, V> {

    public static final String INDEX_EXT = "bwi";
    public static final String VALUES_EXT = "bwv";

    private final String dir;
    private final String repoName;

    private File indexFile;
    private File valuesFile;
    private ValuesFile valuesFileWrapper;

    private HashMap<T, KeyEntry<T>> indexMap;

    public FileKeyValueRepository(String dir, String repoName) throws IOException {
        this.dir = dir;
        this.repoName = repoName;
        initFileReferences();
        loadIndex();
    }

    private void initFileReferences() throws IOException {
        indexFile = new File(dir, repoName + "." + INDEX_EXT);
        if (!indexFile.exists()) {
            if (!indexFile.createNewFile()) {
                throw new IllegalStateException("Cannot create file " + indexFile.getAbsolutePath());
            }
        }

        valuesFile = new File(dir, repoName + "." + VALUES_EXT);
        if (!valuesFile.exists()) {
            if (!valuesFile.createNewFile()) {
                throw new IllegalStateException("Cannot create file " + valuesFile.getAbsolutePath());
            }
        }

        valuesFileWrapper = new ValuesFile(valuesFile);
    }

    private void loadIndex() throws IOException {
        if (indexFile.length() == 0) {
            indexMap = new HashMap<>();
        } else {

            try (ObjectInputStream indexStream = new ObjectInputStream(new FileInputStream(indexFile))) {
                try {
                    indexMap = (HashMap<T, KeyEntry<T>>) indexStream.readObject();
                } catch (ClassNotFoundException e) {
                    throw new IOException("Cannot load index", e);
                }
            }
        }
    }

    @Override
    public synchronized V put(T key, V value) throws IOException {
        KeyEntry<T> keyEntry = indexMap.get(key);
        if (keyEntry != null) {
            remove(key);
        }
        KeyEntry<T> newKeyEntry = writeValue(key, value);
        indexMap.put(key, newKeyEntry);
        return value;
    }

    private KeyEntry<T> writeValue(T key, V value) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);

        RandomAccessFile randomAccessFile = valuesFileWrapper.getFile();
        long pos = randomAccessFile.length();
        randomAccessFile.seek(pos);
        randomAccessFile.write(byteArrayOutputStream.toByteArray());

        int size = byteArrayOutputStream.size();

        return new KeyEntry<T>(key, pos, size);
    }

    @Override
    public synchronized V read(T key) throws IOException {
        KeyEntry<T> keyEntry = indexMap.get(key);
        if (keyEntry == null) return null;

        RandomAccessFile randomAccessFile = valuesFileWrapper.getFile();

        randomAccessFile.seek(keyEntry.getPos());

        byte[] objectArray = new byte[keyEntry.getSize()];
        int readBytes = randomAccessFile.read(objectArray);
        if (readBytes != keyEntry.getSize()) {
            throw new IOException("Cannot read full object. Read only " + readBytes + " bytes of " + keyEntry.getSize());
        }
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(objectArray));
        try {
            return (V) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Cannot read object", e);
        }
    }

    @Override
    public synchronized boolean contains(T key) {
        return indexMap.containsKey(key);
    }

    @Override
    public synchronized V remove(T key) throws IOException {
        if (!contains(key)) return null;
        V object = read(key);
        indexMap.remove(key);
        return object;
    }

    @Override
    public synchronized void clear() throws IOException {
        valuesFileWrapper.getFile().getChannel().truncate(0);
        indexMap.clear();

    }

    @Override
    public synchronized void close() throws IOException {
        valuesFileWrapper.close();

    }

    @Override
    public synchronized void save() throws IOException {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(indexFile))) {
            outputStream.writeObject(indexMap);
        }

    }

    private static class ValuesFile {

        private static final String MODE = "rw";
        private final RandomAccessFile file;

        private ValuesFile(File file) throws IOException {
            this.file = new RandomAccessFile(file, MODE);
        }

        public RandomAccessFile getFile() {
            return file;
        }

        public void close() throws IOException {
            file.close();
        }
    }
}
