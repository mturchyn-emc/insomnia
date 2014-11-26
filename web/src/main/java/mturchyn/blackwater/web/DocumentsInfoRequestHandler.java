package mturchyn.blackwater.web;

import mturchyn.blackwater.core.config.DocumentDescriptor;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class DocumentsInfoRequestHandler {

    private ObjectMapper jsonMapper = new ObjectMapper();

    public void handleDocumentsInfoRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApplicationContext instance = ApplicationContext.getInstance();
        List<DocumentDescriptor> documents = instance.getSchema().getDocuments();
        writeResponse(response, documents);
    }

    private void writeResponse(HttpServletResponse response, Object result) throws IOException {
        response.setHeader("Content-Type", "application/json");
        PrintWriter writer = response.getWriter();
        writer.write(jsonMapper.writeValueAsString(result));
        writer.close();
    }


}
