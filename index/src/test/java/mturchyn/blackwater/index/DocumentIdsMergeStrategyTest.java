package mturchyn.blackwater.index;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DocumentIdsMergeStrategyTest {

    private DocumentIdsMergeStrategy mergeStrategy = new SimpleDocumentIdsMergeStrategy();

    @Test
    public void testMergeThreeEqualLists() throws Exception {
        List<Long> list1 = Arrays.asList(1L, 2L, 3L, 4L);
        List<Long> list2 = Arrays.asList(1L, 2L, 3L, 4L);
        List<Long> list3 = Arrays.asList(1L, 2L, 3L, 4L);

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1, list2, list3));
        Assert.assertEquals(4, mergedIds.size());
        Assert.assertEquals(1L, mergedIds.get(0).longValue());
        Assert.assertEquals(2L, mergedIds.get(1).longValue());
        Assert.assertEquals(3L, mergedIds.get(2).longValue());
        Assert.assertEquals(4L, mergedIds.get(3).longValue());
    }

    @Test
    public void testMergeTwoEmptyLists() throws Exception {
        List<Long> list1 = Arrays.asList();
        List<Long> list2 = Arrays.asList();

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1, list2));
        Assert.assertEquals(0, mergedIds.size());
    }

    @Test
    public void testMergeOnlyOneList() throws Exception {
        List<Long> list1 = Arrays.asList(1L, 2L);

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1));
        Assert.assertEquals(2, mergedIds.size());
    }

    @Test
    public void testMergeOnlyOneEmptyList() throws Exception {
        List<Long> list1 = Arrays.asList();

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1));
        Assert.assertEquals(0, mergedIds.size());
    }

    @Test
    public void testEmptyListWithNonEmptyList() throws Exception {
        List<Long> list1 = Arrays.asList(1L, 2L, 3L);
        List<Long> list2 = Arrays.asList();

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1, list2));
        Assert.assertEquals(0, mergedIds.size());
    }

    @Test
    public void testDecreasingLists() throws Exception {
        List<Long> list1 = Arrays.asList(1L, 2L, 3L, 4L);
        List<Long> list3 = Arrays.asList(1L, 2L);
        List<Long> list2 = Arrays.asList(1L, 2L, 3L);

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1, list2, list3));
        Assert.assertEquals(2, mergedIds.size());
        Assert.assertEquals(1L, mergedIds.get(0).longValue());
        Assert.assertEquals(2L, mergedIds.get(1).longValue());
    }

    @Test
    public void testNonCommonLists() throws Exception {
        List<Long> list1 = Arrays.asList(1L, 2L, 3L);
        List<Long> list3 = Arrays.asList(4L, 5L);
        List<Long> list2 = Arrays.asList(6L, 7L);

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1, list2, list3));
        Assert.assertEquals(0, mergedIds.size());
    }

    @Test
    public void testAllNonCommonLists() throws Exception {
        List<Long> list1 = Arrays.asList(1L, 2L, 3L, 4L);
        List<Long> list3 = Arrays.asList(4L, 5L);
        List<Long> list2 = Arrays.asList(5L, 6L, 7L);

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1, list2, list3));
        Assert.assertEquals(0, mergedIds.size());
    }

    @Test
    public void testListsWithCommonElements() throws Exception {
        List<Long> list1 = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        List<Long> list3 = Arrays.asList(3L, 4L, 5L);
        List<Long> list2 = Arrays.asList(3L, 5L, 7L);

        List<Long> mergedIds = mergeStrategy.merge(Arrays.asList(list1, list2, list3));
        Assert.assertEquals(2, mergedIds.size());
        Assert.assertEquals(3L, mergedIds.get(0).longValue());
        Assert.assertEquals(5L, mergedIds.get(1).longValue());
    }



}
