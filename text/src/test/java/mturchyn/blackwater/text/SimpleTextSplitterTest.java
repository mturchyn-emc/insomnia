package mturchyn.blackwater.text;

import junit.framework.TestCase;
import mturchyn.blackwater.text.SimpleTextSplitter;
import mturchyn.blackwater.text.TextSplitter;
import org.junit.Assert;

public class SimpleTextSplitterTest extends TestCase {


    private TextSplitter splitter = new SimpleTextSplitter();

    public void testSplitEmptyContent() throws Exception {
        String[] strings = splitter.splitContentIntoTokens("");
        Assert.assertTrue(strings.length == 0);
    }

    public void testSplitWordsContent() throws Exception {
        String[] tokens = splitter.splitContentIntoTokens("Asdc aSd");
        Assert.assertTrue(tokens.length == 2);
        Assert.assertEquals("asdc", tokens[0]);
        Assert.assertEquals("asd", tokens[1]);
    }

    public void testSplitWordsWithSyntaxContent() throws Exception {
        String[] tokens = splitter.splitContentIntoTokens("Asdc, aSd, e.");
        Assert.assertTrue(tokens.length == 3);
        Assert.assertEquals("asdc", tokens[0]);
        Assert.assertEquals("asd", tokens[1]);
        Assert.assertEquals("e", tokens[2]);
    }

    public void testOnlySyntaxContent() throws Exception {
        String[] tokens = splitter.splitContentIntoTokens(",  -- \n. .... , -- - - - --- , . ");
        Assert.assertTrue(tokens.length == 0);
    }

    public void testDuplicateContent() throws Exception {
        String[] tokens = splitter.splitContentIntoTokens("asd ASD Asd aSd aSD");
        Assert.assertEquals(1, tokens.length);
        Assert.assertEquals("asd", tokens[0]);
    }

    public void testSimpleTextContent() throws Exception {
        String content =
          "He likes to play, sing and dance.\n" +
            "\tLast year he even took part in a festival - \"Annual dance festival: 2014 New York Event\"";
        String[] tokens = splitter.splitContentIntoTokens(content);
        String[] expectedTokens = {
          "likes", "play", "sing", "dance",
          "last", "year", "even", "took", "part", "festival",
          "annual", "2014", "new", "york", "event"
        };
        Assert.assertTrue(tokens.length == expectedTokens.length);
        for (int i = 0; i < expectedTokens.length; i++) {
            Assert.assertEquals(expectedTokens[i], tokens[i]);
        }
    }
}