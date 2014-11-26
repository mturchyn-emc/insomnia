package mturchyn.blackwater.index.memory;

import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.index.DocumentRepository;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDocumentRepository implements DocumentRepository {

    private ConcurrentHashMap<Long, Document> documents = new ConcurrentHashMap<>();

    @Override
    public void put(Document doc) {
        documents.putIfAbsent(doc.getId(), doc);
    }

    @Override
    public Document getById(long id) {
        return documents.get(id);
    }

    @Override
    public void clear() {
        documents.clear();
    }

    @Override
    public void removeById(long id) {
        documents.remove(id);
    }

    @Override
    public void close() {
        // Do nothing.
    }
}
