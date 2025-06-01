import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextGraphAppWhiteBoxTest {
    private Main app;

    @Before
    public void setUp() {
        app = new Main();
        String testFilePath = "D:\\Software_Lab\\Software_Lab3\\textfile\\easy_test.txt";
        app.initForTest(testFilePath);
    }

    @Test
    public void testUninitializedGraph() {
        Main newApp = new Main();
        String result = newApp.queryBridgeWords("any", "word");
        assertEquals("图未构建", result);
    }

    @Test
    public void testEmptyInput() {
        String result = app.queryBridgeWords("detailed", "");
        assertEquals("Input Error!", result);
    }

    @Test
    public void testnoneWord() {
        String result = app.queryBridgeWords("the", "nothing");
        assertEquals("No the or nothing in the graph!", result);
    }

    @Test
    public void testSameWord() {
        String result = app.queryBridgeWords("data", "it");
        assertEquals("No bridge words from data to it!", result);
    }

    @Test
    public void correct_test() {
        String result = app.queryBridgeWords("carefully", "the");
        assertEquals("The bridge words from carefully to the are: analyzed.", result);
    }

    @Test
    public void testMultipleBridgeWords() {
        String result = app.queryBridgeWords("analyzed", "data");
        assertEquals("The bridge words from analyzed to data are: the, and more.", result);
    }

}
