package mturchyn.blackwater.integration;

import java.util.List;

public interface NoteDao {

    List<Note> readAllNotes();

    void removeAll();

    void insertNotes(List<Note> notes);

}
