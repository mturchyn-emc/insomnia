package mturchyn.blackwater.index;

public class Request {

    String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "Request{" +
          "query='" + query + '\'' +
          '}';
    }
}
