package mturchyn.blackwater.index.file;

import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.core.exceptions.ErrorCode;
import mturchyn.blackwater.core.exceptions.SystemException;
import mturchyn.blackwater.file.keyvalue.FileKeyValueRepository;
import mturchyn.blackwater.file.keyvalue.KeyValueRepository;
import mturchyn.blackwater.index.DocumentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FileDocumentRepository implements DocumentRepository {

    private final static Logger LOG = LogManager.getLogger(FileDocumentRepository.class);
    private KeyValueRepository<Long, Document> repository;

    public FileDocumentRepository(String dir, String indexName) throws IOException {
        this.repository = new FileKeyValueRepository<>(dir, indexName + "_docs");
    }

    @Override
    public void put(Document doc) {
        try {
            repository.put(doc.getId(), doc);
        } catch (IOException e) {
            LOG.error("Error occurred saving the document", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot save the document", e);
        }

    }

    @Override
    public Document getById(long id) {
        try {
            return repository.read(id);
        } catch (IOException e) {
            LOG.error("Error occurred reading the document", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot read the document by id", e);
        }
    }

    @Override
    public void clear() {
        try {
            repository.clear();
        } catch (IOException e) {
            LOG.error("Error occurred clearing the repository", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot clear the repository", e);
        }

    }

    @Override
    public void removeById(long id) {
        try {
            repository.remove(id);
        } catch (IOException e) {
            LOG.error("Error occurred removing the document", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot remove document", e);
        }

    }

    @Override
    public void close() {
        try {
            repository.save();
            repository.close();
        } catch (IOException e) {
            LOG.error("Error occurred closing repository", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot close repository", e);
        }
    }
}
