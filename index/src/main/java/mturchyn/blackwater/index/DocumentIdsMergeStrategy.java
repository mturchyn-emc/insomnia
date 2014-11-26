package mturchyn.blackwater.index;

import java.util.List;

public interface DocumentIdsMergeStrategy {

    /**
     * Merge document ids lists into one list.
     *
     * @param documentIdsList list of document ids in ASC order
     * @return merge document ids
     */
    List<Long> merge(List<List<Long>> documentIdsList);

}
