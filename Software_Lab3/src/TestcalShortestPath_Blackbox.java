import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestcalShortestPath_Blackbox {
    private Main app;

    @Before
    public void setUp() {
        app = new Main();
        String testFilePath = "D:\\Software_Lab\\Software_Lab3\\textfile\\easy_test.txt";
        app.initForTest(testFilePath);
    }

    @Test
    public void testEmptyInput() {
        String result = app.calcShortestPath("Scientist", "");
        assertTrue("Output should contain path", result.contains("scientist -> analyzed -> the"));
        assertTrue("Output should contain path", result.contains("scientist -> carefully"));
    }

    @Test
    public void testnoneWord() {
        String result = app.calcShortestPath("shared", "the");
        assertTrue("Output should contain path", result.contains("shared -> the"));
        assertTrue("Output should contain length", result.contains("(length: 1)"));
    }

    @Test
    public void testSameWord() {
        String result = app.calcShortestPath("", "");
        assertEquals("请输入至少一个单词", result);
    }

    @Test
    public void correct_test() {
        String result = app.calcShortestPath("shared", "nothing");
        assertEquals("No nothing in the graph!", result);
    }

}
