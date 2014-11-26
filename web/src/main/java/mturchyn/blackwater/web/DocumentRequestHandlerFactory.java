package mturchyn.blackwater.web;

import mturchyn.blackwater.core.config.DocumentDescriptor;
import mturchyn.blackwater.core.config.Settings;
import mturchyn.blackwater.core.exceptions.ErrorCode;
import mturchyn.blackwater.core.exceptions.SystemException;
import mturchyn.blackwater.index.DocumentRepository;
import mturchyn.blackwater.index.file.FileDocumentRepository;
import mturchyn.blackwater.index.file.FileInvertedIndex;
import mturchyn.blackwater.index.IndexReader;
import mturchyn.blackwater.index.IndexWriter;
import mturchyn.blackwater.index.InvertedIndex;
import mturchyn.blackwater.text.SimpleTextSplitter;
import mturchyn.blackwater.text.TextSplitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DocumentRequestHandlerFactory {
    private static final Logger LOG = LogManager.getLogger(IndexReader.class.getName());
    private static final String DATA_FOLDER_NAME = "data";

    private List<Closable> closables = new LinkedList<>();

    public static DocumentRequestHandlerFactory instance() {
        return new DocumentRequestHandlerFactory();
    }

    public DocumentRequestHandler createDocumentRequestHandler(DocumentDescriptor documentDescriptor, Settings settings) {
        try {
            ApplicationContext applicationContext = ApplicationContext.getInstance();

            String dataDir = applicationContext.getHomeDir() + DATA_FOLDER_NAME;
            final InvertedIndex index = new FileInvertedIndex(dataDir, documentDescriptor.getName());
            closables.add(index::close);
            final DocumentRepository documentRepository = new FileDocumentRepository(dataDir, documentDescriptor.getName());
            closables.add(documentRepository::close);

            TextSplitter textSplitter = new SimpleTextSplitter();
            ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
            IndexReader indexReader = new IndexReader(index, documentRepository, textSplitter, readWriteLock);
            IndexWriter indexWriter = new IndexWriter(index, documentRepository, textSplitter, readWriteLock);
            return new StandardDocumentRequestHandler(documentDescriptor, indexReader, indexWriter);
        } catch (IOException e) {
            LOG.error("Error occurred while initializing document request handler", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR, "Cannot create document request handler", e);
        }
    }

    public void close() {
        for (Closable closable : closables) {
            try {
                closable.close();
            } catch (Exception e) {
                LOG.error("Cannot close instance", e);
            }
        }
    }

    private static interface Closable {

        void close();

    }


}
