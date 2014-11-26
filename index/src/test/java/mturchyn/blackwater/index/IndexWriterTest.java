package mturchyn.blackwater.index;

import mturchyn.blackwater.core.config.DocumentDescriptor;
import mturchyn.blackwater.core.config.FieldDescriptor;
import mturchyn.blackwater.core.config.FieldType;
import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.core.document.Field;
import mturchyn.blackwater.text.SimpleTextSplitter;
import mturchyn.blackwater.text.TextSplitter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IndexWriterTest {

    private IndexWriter writer;
    private DocumentRepository mockDocumentRepository;
    private InvertedIndex mockInvertedIndex;
    private TextSplitter textSplitter = new SimpleTextSplitter();

    @Before
    public void setUp() throws Exception {
        mockDocumentRepository = Mockito.mock(DocumentRepository.class);
        mockInvertedIndex = Mockito.mock(InvertedIndex.class);
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        writer = new IndexWriter(mockInvertedIndex, mockDocumentRepository, textSplitter, readWriteLock);
    }

    @Test
    public void testFlow() {
        DocumentDescriptor docDesc = new DocumentDescriptor();
        docDesc.setName("notes");
        docDesc.setFields(Arrays.asList(
          fieldDescr("ID", FieldType.LONG, false),
          fieldDescr("Content", FieldType.TEXT, true),
          fieldDescr("Title", FieldType.TEXT, true),
          fieldDescr("AUTHOR", FieldType.TEXT, false)
        ));

        Document doc = new Document();
        doc.setId(1L);
        doc.setFields(Arrays.asList(
          Field.from("id", 1L),
          Field.from("title", "Note abc abc"),
          Field.from("content", "Bla BLA bla1 bla2"),
          Field.from("Author", "mturchyn")
        ));

        writer.addDocument(doc, docDesc);

        Mockito.verify(mockInvertedIndex).put("note", 1L);
        Mockito.verify(mockInvertedIndex).put("abc", 1L);
        Mockito.verify(mockInvertedIndex).put("bla", 1L);
        Mockito.verify(mockInvertedIndex).put("bla1", 1L);
        Mockito.verify(mockInvertedIndex).put("bla2", 1L);

        Mockito.verify(mockDocumentRepository).put(doc);

    }

    private FieldDescriptor fieldDescr(String name, FieldType type, boolean indexed) {
        FieldDescriptor fieldDescriptor = new FieldDescriptor();
        fieldDescriptor.setName(name);
        fieldDescriptor.setType(type);
        fieldDescriptor.setIndexed(indexed);
        return fieldDescriptor;
    }
}
