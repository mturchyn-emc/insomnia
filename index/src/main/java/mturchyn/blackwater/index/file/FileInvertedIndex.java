package mturchyn.blackwater.index.file;

import mturchyn.blackwater.core.exceptions.ErrorCode;
import mturchyn.blackwater.core.exceptions.SystemException;
import mturchyn.blackwater.file.keyvalue.FileKeyValueRepository;
import mturchyn.blackwater.file.keyvalue.KeyValueRepository;
import mturchyn.blackwater.index.InvertedIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FileInvertedIndex implements InvertedIndex {

    private final static Logger LOG = LogManager.getLogger(FileInvertedIndex.class);
    private KeyValueRepository<String, List<Long>> repository;

    public FileInvertedIndex(String dir, String indexName) throws IOException {
        this.repository = new FileKeyValueRepository<>(dir, indexName + "_terms");
    }

    @Override
    public void clear() {
        try {
            repository.clear();
        } catch (IOException e) {
            LOG.error("Error occurred while clearing terms", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot clear terms", e);
        }
    }

    @Override
    public void put(String term, long docId) {
        try {
            doPut(term, docId);
        } catch (IOException e) {
            LOG.error("Error occurred putting the term", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot put new term", e);
        }

    }

    private void doPut(String term, long docId) throws IOException {
        List<Long> ids = repository.read(term);
        if (ids != null) {
            ids.add(docId);
            Collections.sort(ids);
        } else {
            List<Long> docIds = new LinkedList<>();
            docIds.add(docId);
            repository.put(term, docIds);
        }
    }

    @Override
    public List<Long> getDocIdsByTerm(String term) {
        try {
            return repository.read(term);
        } catch (IOException e) {
            LOG.error("Error occurred while reading the term", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot read list of document ids", e);
        }
    }

    @Override
    public void close() {
        try {
            repository.save();
            repository.close();
        } catch (IOException e) {
            LOG.error("Error occurred while closing the index", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot close the index", e);
        }

    }
}
