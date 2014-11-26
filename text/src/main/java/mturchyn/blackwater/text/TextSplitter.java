package mturchyn.blackwater.text;

import java.lang.String;

public interface TextSplitter {
    String[] splitContentIntoTokens(String content);
}
