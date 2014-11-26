package mturchyn.blackwater.index;

import mturchyn.blackwater.core.document.Document;

public interface DocumentRepository {

    void put(Document doc);

    Document getById(long id);

    void clear();

    void removeById(long id);

    void close();


}
