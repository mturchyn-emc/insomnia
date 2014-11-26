package mturchyn.blackwater.web;

import mturchyn.blackwater.core.config.DocumentDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestDispatcher extends HttpServlet {

    private final static Logger LOG = LogManager.getLogger(RequestDispatcher.class);

    private ApplicationContext applicationContext;
    private Map<String, DocumentRequestHandler> documentHandlers;
    private DocumentsInfoRequestHandler documentsInfoRequestHandler;
    private ObjectMapper jsonMapper = new ObjectMapper();
    private DocumentRequestHandlerFactory documentRequestHandlerFactory = DocumentRequestHandlerFactory.instance();

    @Override
    public void init(ServletConfig config) throws ServletException {
        LOG.info("Request dispatcher initialization.");
        super.init(config);
        applicationContext = ApplicationContext.init(getHomeDir());
        initDocumentHandlers();
        documentsInfoRequestHandler = new DocumentsInfoRequestHandler();
        LOG.info("Request dispatcher initialization finished.");
    }

    private void initDocumentHandlers() {
        List<DocumentDescriptor> documents = applicationContext.getSchema().getDocuments();
        documentHandlers = new HashMap<>(documents.size());

        for (DocumentDescriptor documentDescriptor : documents) {
            LOG.info("Creating request handler for document: {}", documentDescriptor.getName());
            DocumentRequestHandler documentRequestHandler = documentRequestHandlerFactory.createDocumentRequestHandler(
              documentDescriptor,
              applicationContext.getSettings()
            );
            documentHandlers.put(documentDescriptor.getName(), documentRequestHandler);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String documentName = extractDocName(request);
        if ("".equalsIgnoreCase(documentName)) {
            documentsInfoRequestHandler.handleDocumentsInfoRequest(request, response);
        } else {
            DocumentRequestHandler documentRequestHandler = documentHandlers.get(documentName);
            if (documentRequestHandler == null) {
                handleDocumentNotFoundError(response, documentName);
            } else {
                documentRequestHandler.searchForDocuments(request, response);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String documentName = extractDocName(request);
        DocumentRequestHandler documentRequestHandler = documentHandlers.get(documentName);
        if (documentRequestHandler == null) {
            handleDocumentNotFoundError(response, documentName);
        } else {
            documentRequestHandler.addDocuments(request, response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String documentName = extractDocName(request);
        DocumentRequestHandler documentRequestHandler = documentHandlers.get(documentName);
        if (documentRequestHandler == null) {
            handleDocumentNotFoundError(response, documentName);
        } else {
            documentRequestHandler.cleanIndex(request, response);
        }
    }

    private void handleDocumentNotFoundError(HttpServletResponse response, String documentName) throws IOException {
        LOG.debug("Document {} not found", documentName);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setHeader("Content-Type", "application/json");
        Error notFoundError = Error.of("Document not found", String.format("Document with name [%s] doesn't exist", documentName));
        PrintWriter writer = response.getWriter();
        writer.write(jsonMapper.writeValueAsString(notFoundError));
        writer.close();
    }

    private String extractDocName(HttpServletRequest request) {
        String documentName = request.getPathInfo();
        if (documentName.startsWith("/")) {
            return documentName.substring(1);
        } else {
            return documentName;
        }
    }

    @Override
    public void destroy() {
        LOG.info("Closing the context..");
        documentRequestHandlerFactory.close();
        LOG.info("Context is closed.");
    }

    private String getHomeDir() {
        if (System.getProperty("blackwater.home") != null) {
            return System.getProperty("blackwater.home");
        } else {
            return System.getProperty("user.home");
        }
    }
}