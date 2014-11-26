package mturchyn.blackwater.integration;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.util.List;

public class NoteJdbcDao implements NoteDao {

    private JdbcTemplate jdbcTemplate;

    public NoteJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Note> readAllNotes() {
        return jdbcTemplate.query("SELECT * FROM NOTES", (ResultSet resultSet, int i) -> {
            Note note = new Note();
            note.setId(resultSet.getLong("ID"));
            note.setTitle(resultSet.getString("TITLE"));
            note.setContent(resultSet.getString("CONTENT"));
            return note;
        });
    }

    @Override
    public void removeAll() {
        jdbcTemplate.update("DELETE FROM NOTES");
    }

    @Override
    public void insertNotes(List<Note> notes) {
        for (Note note : notes) {
            String sql = "INSERT INTO NOTES (ID, USER_ID, TITLE, CONTENT) VALUES (?, ?, ?, ?)";

            jdbcTemplate.update(sql, new Object[] {
              note.getId(),
              1L,
              note.getTitle(),
              note.getContent()
            });
        }
    }
}
