package mturchyn.blackwater.index;

import java.lang.Long;import java.lang.String;import java.util.List;

public interface InvertedIndex {

    void clear();

    void put(String term, long docId);

    List<Long> getDocIdsByTerm(String term);

    void close();
}
