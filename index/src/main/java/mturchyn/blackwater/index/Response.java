package mturchyn.blackwater.index;

import mturchyn.blackwater.core.document.Document;

import java.util.List;

public class Response {

    private Request request;
    private long takenTimeMs;
    private List<Document> documents;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public long getTakenTimeMs() {
        return takenTimeMs;
    }

    public void setTakenTimeMs(long takenTimeMs) {
        this.takenTimeMs = takenTimeMs;
    }
}
