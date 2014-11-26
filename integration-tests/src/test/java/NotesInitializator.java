import mturchyn.blackwater.integration.EnglishWords;
import mturchyn.blackwater.integration.Note;
import mturchyn.blackwater.integration.NoteDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotesInitializator {

    private ConfigurableApplicationContext context;
    private NoteDao noteDao;
    private Random random = new Random();
    private EnglishWords englishWords;


    @Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("integration-context.xml");
        noteDao = (NoteDao) context.getBean("noteDao");
        englishWords = EnglishWords.getInstance();
    }

    @Test
    public void removeAllAndInitNotes() throws Exception {
        noteDao.removeAll();
        int n = 100_000;
        List<Note> notes = new ArrayList<>(n);
        Note note;
        for (long i = 1; i <= n; i++) {
            note = new Note();
            note.setId(i);
            note.setTitle(generateTitle());
            note.setContent(generateContent());
            notes.add(note);
        }
        noteDao.insertNotes(notes);
    }

    private String generateContent() {
        String content = "";
        for (int j = 0; j < 1 + random.nextInt(100); j++) {
            content += englishWords.nextRandomWord();
            content += " ";
        }
        return content.trim();
    }

    private String generateTitle() {
        String content = "";
        for (int j = 0; j < 1 + random.nextInt(8); j++) {
            content += englishWords.nextRandomWord();
            content += " ";
        }
        return content.trim();
    }

    @After
    public void tearDown() throws Exception {
        context.close();
    }
}
