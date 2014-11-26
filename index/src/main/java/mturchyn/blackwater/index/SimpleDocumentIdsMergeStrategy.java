package mturchyn.blackwater.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleDocumentIdsMergeStrategy implements DocumentIdsMergeStrategy {

    public List<Long> merge(List<List<Long>> documentIdsList) {

        if (documentIdsList.isEmpty()) return Collections.emptyList();

        if (documentIdsList.size() == 1) return documentIdsList.get(0);



        documentIdsList.sort((List<Long> list1, List<Long> list2) -> list1.size() - list2.size());

        if (documentIdsList.get(0).isEmpty()) return Collections.emptyList();

        List<Long> mergedIds = documentIdsList.get(0);

        for (int i = 1; i < documentIdsList.size(); i++) {
            List<Long> nextDocumentIds = documentIdsList.get(i);

            List<Long> smallerSet;
            List<Long> biggerSet;

            if (nextDocumentIds.size() < mergedIds.size()) {
                smallerSet = nextDocumentIds;
                biggerSet = mergedIds;
            } else {
                smallerSet = mergedIds;
                biggerSet = nextDocumentIds;
            }

            List<Long> newMergedIds = new ArrayList<>(smallerSet.size());

            int smallerSetIndex = 0;
            int biggerSetIndex = 0;

            while (smallerSetIndex < smallerSet.size() && biggerSetIndex < biggerSet.size()) {
                Long smallerDocumentId = smallerSet.get(smallerSetIndex);
                Long biggerDocumentId = biggerSet.get(biggerSetIndex);
                if (smallerDocumentId.equals(biggerDocumentId)) {
                    newMergedIds.add(smallerDocumentId);
                    biggerSetIndex++;
                    smallerSetIndex++;
                } else if (smallerDocumentId < biggerDocumentId) {
                    smallerSetIndex++;
                } else {
                    biggerSetIndex++;
                }
            }

            mergedIds = newMergedIds;
        }

        return mergedIds;

    }

}
