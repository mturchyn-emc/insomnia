package mturchyn.blackwater.index.memory;

import mturchyn.blackwater.index.InvertedIndex;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInvertedIndex implements InvertedIndex {

    private ConcurrentHashMap<String, List<Long>> termsDocMap = new ConcurrentHashMap<>();

    @Override
    public void clear() {
        termsDocMap.clear();
    }

    @Override
    public void put(String term, long docId) {
        if (termsDocMap.containsKey(term)) {
            List<Long> docIds = termsDocMap.get(term);
            docIds.add(docId);
            Collections.sort(docIds);
        } else {
            List<Long> docIds = new LinkedList<>();
            docIds.add(docId);
            termsDocMap.put(term, docIds);
        }

    }

    @Override
    public List<Long> getDocIdsByTerm(String term) {
        List<Long> docIds = termsDocMap.get(term);
        if (docIds == null) {
            return Collections.emptyList();
        } else {
            return docIds;
        }
    }

    @Override
    public void close() {
        // Do nothing.
    }
}
