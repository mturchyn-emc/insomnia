package mturchyn.blackwater.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnglishWords {

    private static final String FILE_NAME = "english-words.txt";

    private static final EnglishWords instance = new EnglishWords();

    private List<String> englishWords = new ArrayList<>(3000);
    private Random random = new Random();

    public static EnglishWords getInstance() {
        return instance;
    }

    private EnglishWords() {
        try {
            readWordsFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readWordsFromFile() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
        // Construct BufferedReader from FileReader
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = br.readLine()) != null) {
            englishWords.add(line);
        }

        br.close();
    }

    public List<String> getEnglishWords() {
        return englishWords;
    }

    public String nextRandomWord() {
        return englishWords.get(random.nextInt(englishWords.size()));
    }
}
