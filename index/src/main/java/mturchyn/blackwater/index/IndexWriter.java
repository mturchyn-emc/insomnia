package mturchyn.blackwater.index;

import mturchyn.blackwater.core.config.DocumentDescriptor;
import mturchyn.blackwater.core.config.FieldDescriptor;
import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.core.document.Field;
import mturchyn.blackwater.text.TextSplitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class IndexWriter {
    private final static Logger LOG = LogManager.getLogger(IndexWriter.class);

    private InvertedIndex invertedIndex;
    private DocumentRepository documentRepository;
    private TextSplitter textSplitter;
    private Lock indexWriteLock;

    public IndexWriter(InvertedIndex invertedIndex, DocumentRepository documentRepository,
                       TextSplitter textSplitter, ReadWriteLock readWriteLock) {
        this.invertedIndex = invertedIndex;
        this.indexWriteLock = readWriteLock.writeLock();
        this.documentRepository = documentRepository;
        this.textSplitter = textSplitter;
    }

    /**
     * Clean index.
     */
    public void clearIndex() {
        indexWriteLock.lock();
        try {
            invertedIndex.clear();
            documentRepository.clear();
        } finally {
            indexWriteLock.unlock();
        }
    }

    /**
     * Parses and saves the document.
     *
     * @param doc document to save
     * @param docDesc the document descriptor
     */
    public void addDocument(Document doc, DocumentDescriptor docDesc) {
        Set<String> terms = parseAllTermsFromDocument(doc, docDesc);
        LOG.trace("Putting terms into index");
        indexWriteLock.lock();
        try {
            putTermsIntoIndex(doc, terms);
            LOG.trace("Putting document into repository");
            documentRepository.put(doc);
        } finally {
            indexWriteLock.unlock();
        }
        LOG.trace("Writing document is finished.");
    }

    private Set<String> parseAllTermsFromDocument(Document doc, DocumentDescriptor docDesc) {
        Set<String> terms = new LinkedHashSet<>();
        for (Field field : doc.getFields()) {
            if (isIndexed(field, docDesc)) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Field " + field.getName() + " is indexed");
                }
                String[] tokens = textSplitter.splitContentIntoTokens((String) field.getValue());
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Tokens are\n" + Arrays.toString(tokens));
                }
                terms.addAll(Arrays.asList(tokens));
            }
        }
        return terms;
    }

    private boolean isIndexed(Field field, DocumentDescriptor docDesc) {
        for (FieldDescriptor descriptor : docDesc.getFields()) {
            if (field.getName().equalsIgnoreCase(descriptor.getName()) && descriptor.isIndexed()) {
                return true;
            }
        }
        return false;
    }

    private void putTermsIntoIndex(Document doc, Set<String> terms) {
        long docId = doc.getId();
        for (String term : terms) {
            invertedIndex.put(term, docId);
        }
    }
}
