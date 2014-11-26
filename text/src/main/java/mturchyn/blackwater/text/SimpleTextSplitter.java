package mturchyn.blackwater.text;

import java.util.LinkedHashSet;

public class SimpleTextSplitter implements TextSplitter {

    private StopWords stopWords = new StopWords();

    @Override
    public String[] splitContentIntoTokens(String content) {
        if (content == null || content.isEmpty()) return new String[0];
        String[] initialTokens = content.split("\\s");

        LinkedHashSet<String> resultTokens = new LinkedHashSet<>(initialTokens.length);

        for (String initialToken: initialTokens) {
            String tokenCandidate = initialToken.replaceAll("[^\\w]", "");
            tokenCandidate = tokenCandidate.toLowerCase().trim();
            if (isNotStopWord(tokenCandidate) && !resultTokens.contains(tokenCandidate)) {
                resultTokens.add(tokenCandidate);
            }
        }

        return resultTokens.toArray(new String[resultTokens.size()]);

    }

    private boolean isNotStopWord(String tokenCandidate) {
        return !stopWords.isStopWord(tokenCandidate);
    }

}
