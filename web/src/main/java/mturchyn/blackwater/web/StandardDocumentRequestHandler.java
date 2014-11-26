package mturchyn.blackwater.web;

import mturchyn.blackwater.core.config.DocumentDescriptor;
import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.index.IndexReader;
import mturchyn.blackwater.index.IndexWriter;
import mturchyn.blackwater.index.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class StandardDocumentRequestHandler implements DocumentRequestHandler {

    private final static Logger LOG = LogManager.getLogger(RequestDispatcher.class);

    private static final Object DUMMY_OBJECT = new Object();

    private final DocumentDescriptor documentDescriptor;
    private final IndexReader indexReader;
    private final IndexWriter indexWriter;
    private ObjectMapper jsonMapper = new ObjectMapper();

    public StandardDocumentRequestHandler(DocumentDescriptor documentDescriptor,
                                          IndexReader indexReader,
                                          IndexWriter indexWriter) {
        this.documentDescriptor = documentDescriptor;
        this.indexReader = indexReader;
        this.indexWriter = indexWriter;
    }

    @Override
    public void searchForDocuments(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object result = DUMMY_OBJECT;

        String query = request.getParameter("query");
        try {
            if (query == null || query.isEmpty()) {
                result = Error.of("Bad request", "Query cannot be empty.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                Request req = new Request();
                req.setQuery(query);
                result = indexReader.readDocuments(req);
            }

        } catch (Exception e) {
            LOG.error("Error occurred while searching for document \"{}\" with query \"{}\"", documentDescriptor.getName(), query);
            result = Error.of("Internal error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            writeResponse(response, result);
        }
    }

    @Override
    public void addDocuments(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        List<Document> documents = parseDocumentsFromRequest(inputStream);

        for (Document document : documents) {
            indexWriter.addDocument(document, documentDescriptor);
        }

    }

    @Override
    public void cleanIndex(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        indexWriter.clearIndex();
    }

    private void writeResponse(HttpServletResponse response, Object result) throws IOException {
        response.setHeader("Content-Type", "application/json");
        PrintWriter writer = response.getWriter();
        writer.write(jsonMapper.writeValueAsString(result));
        writer.close();
    }

    private List<Document> parseDocumentsFromRequest(ServletInputStream inputStream) throws IOException {
        return jsonMapper.readValue(
          inputStream,
          TypeFactory.defaultInstance().constructCollectionType(
            List.class,
            Document.class
          ));
    }
}
