package mturchyn.blackwater.index;

import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.core.document.Field;
import mturchyn.blackwater.index.file.FileDocumentRepository;
import mturchyn.blackwater.index.memory.InMemoryDocumentRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class DocumentRepositoryTest {

    public static final String DIR = "c:\\Users\\Maksim\\monica\\data";

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String AUTHOR = "Author";
    private DocumentRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new FileDocumentRepository(DIR, "note");
    }

    @Test
    public void testFlow() {
        repository.clear();

        Document nonExisting = repository.getById(1L);
        Assert.assertNull(nonExisting);

        repository.removeById(1L);

        repository.clear();

        Document doc = doc(1L, "Note abc abc", "Bla BLA bla1 bla2", "mturchyn");
        repository.put(doc);

        Document doc2 = doc(2L, "Note abc abc2", "Bl2", "mturchyn");
        repository.put(doc2);

        Document doc1 = repository.getById(1L);
        verifyDoc1(doc1);

        doc1 = repository.getById(1L);
        verifyDoc1(doc1);

        doc2 = repository.getById(2L);
        verifyDoc2(doc2);

        repository.removeById(1L);

        Assert.assertNull(repository.getById(1L));

        repository.put(doc);

        doc1 = repository.getById(1L);
        verifyDoc1(doc1);

        doc2 = repository.getById(2L);
        verifyDoc2(doc2);

        repository.clear();

        Assert.assertNull(repository.getById(1L));
        Assert.assertNull(repository.getById(2L));

    }

    private void verifyDoc2(Document doc1) {
        Assert.assertEquals(2L, doc1.getId());
        Assert.assertEquals("Note abc abc2", getFieldValue(doc1, TITLE));
        Assert.assertEquals("Bl2", getFieldValue(doc1, CONTENT));
        Assert.assertEquals("mturchyn", getFieldValue(doc1, AUTHOR));
    }

    private void verifyDoc1(Document doc1) {
        Assert.assertEquals(1L, doc1.getId());
        Assert.assertEquals("Note abc abc", getFieldValue(doc1, TITLE));
        Assert.assertEquals("Bla BLA bla1 bla2", getFieldValue(doc1, CONTENT));
        Assert.assertEquals("mturchyn", getFieldValue(doc1, AUTHOR));
    }

    private Object getFieldValue(Document doc1, String fieldName) {
        for (Field field : doc1.getFields()) {
            if (field.getName().equals(fieldName)) {
                return field.getValue();
            }
        }
        return null;
    }

    private Document doc(long id, String title, String content, String author) {
        Document doc = new Document();
        doc.setId(id);
        doc.setFields(Arrays.asList(
          Field.from(ID, id),
          Field.from(TITLE, title),
          Field.from(CONTENT, content),
          Field.from(AUTHOR, author)
        ));
        return doc;
    }

    @After
    public void tearDown() throws Exception {
        repository.clear();
        repository.close();


    }
}
