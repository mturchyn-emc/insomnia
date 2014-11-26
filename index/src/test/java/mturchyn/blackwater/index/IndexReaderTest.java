package mturchyn.blackwater.index;

import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.core.document.Field;
import mturchyn.blackwater.text.SimpleTextSplitter;
import mturchyn.blackwater.text.TextSplitter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.mockito.Mockito.*;

import static org.mockito.Matchers.*;

public class IndexReaderTest {

    private IndexReader reader;
    private DocumentRepository mockDocumentRepository;
    private InvertedIndex mockInvertedIndex;
    private TextSplitter mockTextSplitter;

    @Before
    public void setUp() throws Exception {
        mockDocumentRepository = Mockito.mock(DocumentRepository.class);
        mockInvertedIndex = Mockito.mock(InvertedIndex.class);
        mockTextSplitter = Mockito.mock(TextSplitter.class);
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        reader = new IndexReader(mockInvertedIndex, mockDocumentRepository, mockTextSplitter, readWriteLock);
    }

    @Test
    public void testFlow() {

        Request req = new Request();
        req.setQuery("nevermind");

        when(mockTextSplitter.splitContentIntoTokens(anyString())).thenReturn(new String[]{"once", "upon", "time"});
        when(mockInvertedIndex.getDocIdsByTerm("once")).thenReturn(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        when(mockInvertedIndex.getDocIdsByTerm("upon")).thenReturn(Arrays.asList(1L, 3L, 5L));
        when(mockInvertedIndex.getDocIdsByTerm("time")).thenReturn(Arrays.asList(2L, 3L));
        Document document = new Document();
        document.setId(3L);
        document.setFields(new ArrayList<>());
        when(mockDocumentRepository.getById(3L)).thenReturn(document);
        Response response = reader.readDocuments(req);
        Assert.assertEquals(req, response.getRequest());
        Assert.assertEquals(1, response.getDocuments().size());

    }

}
