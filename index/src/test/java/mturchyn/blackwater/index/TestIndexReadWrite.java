package mturchyn.blackwater.index;

import mturchyn.blackwater.core.config.DocumentDescriptor;
import mturchyn.blackwater.core.config.FieldDescriptor;
import mturchyn.blackwater.core.config.FieldType;
import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.core.document.Field;
import mturchyn.blackwater.index.memory.InMemoryDocumentRepository;
import mturchyn.blackwater.index.memory.InMemoryInvertedIndex;
import mturchyn.blackwater.text.SimpleTextSplitter;
import mturchyn.blackwater.text.TextSplitter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TestIndexReadWrite {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String AUTHOR = "Author";
    public static final int THREAD_POOL_SIZE = 100;


    private IndexReader reader;
    private IndexWriter writer;
    private DocumentRepository documentRepository;
    private InvertedIndex invertedIndex;
    private TextSplitter textSplitter;

    private ExecutorService executorService;

    private DocumentDescriptor notesDocDescriptor;


    @Before
    public void setUp() throws Exception {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


        documentRepository = new InMemoryDocumentRepository();
        invertedIndex = new InMemoryInvertedIndex();
        textSplitter = new SimpleTextSplitter();
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        reader = new IndexReader(invertedIndex, documentRepository, textSplitter, readWriteLock);
        writer = new IndexWriter(invertedIndex, documentRepository, textSplitter, readWriteLock);

        notesDocDescriptor = new DocumentDescriptor();
        notesDocDescriptor.setName("notes");
        notesDocDescriptor.setFields(Arrays.asList(
          fieldDescr(ID, FieldType.LONG, false),
          fieldDescr(CONTENT, FieldType.TEXT, true),
          fieldDescr(TITLE, FieldType.TEXT, true),
          fieldDescr(AUTHOR, FieldType.TEXT, false)
        ));


        Document doc1 = doc(1L, "ABC abc, wild wild west", "Wild west content", "mturchyn");
        Document doc2 = doc(2L, "ABC abc, wild1 wild west", "Wild west content", "mturchyn1");
        Document doc3 = doc(3L, "Western union, man.\n It's huge", "West block was built...", "mturchyn2");

        writer.addDocument(doc1, notesDocDescriptor);
        writer.addDocument(doc2, notesDocDescriptor);
        writer.addDocument(doc3, notesDocDescriptor);
    }

    @Test
    public void testReadForWild() {
        Request req = new Request();
        req.setQuery("Wild");
        Response response = reader.readDocuments(req);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.getDocuments().size());

        Document document1 = response.getDocuments().get(0);
        Assert.assertEquals(1L, document1.getId());
        Assert.assertEquals("ABC abc, wild wild west", getFieldValue(document1, TITLE));
        Assert.assertEquals("Wild west content", getFieldValue(document1, CONTENT));
        Assert.assertEquals("mturchyn", getFieldValue(document1, AUTHOR));

        Document document2 = response.getDocuments().get(1);
        Assert.assertEquals(2L, document2.getId());
        Assert.assertEquals("ABC abc, wild1 wild west", getFieldValue(document2, TITLE));
        Assert.assertEquals("Wild west content", getFieldValue(document2, CONTENT));
        Assert.assertEquals("mturchyn1", getFieldValue(document2, AUTHOR));


    }

    @Test
    public void testReadForBuilt() {
        Request req = new Request();
        req.setQuery("BUILT");
        Response response = reader.readDocuments(req);
        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getDocuments().size());

        Document document1 = response.getDocuments().get(0);
        Assert.assertEquals(3L, document1.getId());
        Assert.assertEquals("Western union, man.\n It's huge", getFieldValue(document1, TITLE));
        Assert.assertEquals("West block was built...", getFieldValue(document1, CONTENT));
        Assert.assertEquals("mturchyn2", getFieldValue(document1, AUTHOR));
    }

    @Test
    public void testReadForEmptyResult() {
        Request req = new Request();
        req.setQuery("NOSUCHWORD");
        Response response = reader.readDocuments(req);
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getDocuments().size());
    }

    @Test
    public void testConcurrentReadAndWrite() throws Exception {
        int n = 1_000;
        List<Document> docsToAdd = new ArrayList<>(n);
        for (long id = 0L; id < n; id++) {
            docsToAdd.add(doc(id, "Western union, man.\n It's huge", "West block was built...", "mturchyn2"));
        }

        List<Future<?>> readTasks = new ArrayList<>(n);

        for (int i =0; i < 100_00; i++) {
            readTasks.add(executorService.submit(() -> {
                Request req = new Request();
                req.setQuery("block");
                reader.readDocuments(req);
            }));
        }

        List<Future<?>> writeTasks = new ArrayList<>(n);
        for (final Document document : docsToAdd) {
            writeTasks.add(executorService.submit(() -> writer.addDocument(document, notesDocDescriptor)));
        }


        for (Future future : writeTasks) {
            future.get();
        }
        for (Future future : readTasks) {
            future.get();
        }

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

    private FieldDescriptor fieldDescr(String name, FieldType type, boolean indexed) {
        FieldDescriptor fieldDescriptor = new FieldDescriptor();
        fieldDescriptor.setName(name);
        fieldDescriptor.setType(type);
        fieldDescriptor.setIndexed(indexed);
        return fieldDescriptor;
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
    }
}
