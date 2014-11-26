package mturchyn.blackwater.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface DocumentRequestHandler {

    void searchForDocuments(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    void addDocuments(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    void cleanIndex(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
