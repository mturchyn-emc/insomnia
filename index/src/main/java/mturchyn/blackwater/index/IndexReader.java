package mturchyn.blackwater.index;

import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.text.TextSplitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IndexReader {
    private static final Logger LOG = LogManager.getLogger(IndexReader.class.getName());

    private InvertedIndex index;
    private DocumentRepository documentRepository;
    private TextSplitter textSplitter;
    private Lock indexReadLock;

    private DocumentIdsMergeStrategy mergeStrategy = new SimpleDocumentIdsMergeStrategy();

    public IndexReader(InvertedIndex invertedIndex, DocumentRepository documentRepository,
                       TextSplitter textSplitter, ReadWriteLock readWriteLock) {
        this.index = invertedIndex;
        this.indexReadLock = readWriteLock.readLock();
        this.documentRepository = documentRepository;
        this.textSplitter = textSplitter;
    }

    public Response readDocuments(Request req) {
        boolean traceEnabled = LOG.isTraceEnabled();

        Response res = new Response();
        res.setRequest(req);
        if (traceEnabled) {
            LOG.trace("Start reading documents. " + req.toString());
        }
        long start = System.currentTimeMillis();

        indexReadLock.lock();
        List<Document> documents;
        try {
            documents = getCachedDocuments(req);
        } finally {
            indexReadLock.unlock();
        }

        long taken = System.currentTimeMillis() - start;
        res.setTakenTimeMs(taken);
        if (traceEnabled) {
            LOG.trace("Read operation finished in " + taken + " ms");
        }
        res.setDocuments(documents);
        return res;
    }

    private List<Document> getCachedDocuments(Request req) {
        List<Long> finalDocumentIds = getDocumentIds(req);
        List<Document> documents = new ArrayList<>(finalDocumentIds.size());
        for (Long documentId : finalDocumentIds) {
            Document document = documentRepository.getById(documentId);
            documents.add(document);
        }
        return documents;
    }

    private List<Long> getDocumentIds(Request request) {
        String query = request.getQuery();
        String[] tokens = textSplitter.splitContentIntoTokens(query);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Query " + query + " was split into tokens: \n" + Arrays.toString(tokens));
        }

        List<Long> finalDocumentIds;
        if (tokens.length > 0) {
            finalDocumentIds = lookUpDocumentIdsInIndex(tokens);
        } else {
            finalDocumentIds = Collections.emptyList();
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Document ids are " + Arrays.toString(finalDocumentIds.toArray()));
        }

        return finalDocumentIds;
    }

    private List<Long> lookUpDocumentIdsInIndex(String[] tokens) {
        List<Long> finalDocumentIds;
        List<List<Long>> documentIdsList = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            List<Long> documentIds = index.getDocIdsByTerm(token);
            documentIdsList.add(documentIds);
        }

        finalDocumentIds = mergeStrategy.merge(documentIdsList);
        return finalDocumentIds;
    }

}
