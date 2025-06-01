import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class queryBridgeWords_test {
    private static Main app;

    @BeforeClass
    public static void setUpClass() {
        app = new Main();
        // 加载测试文件（相对路径）
        String testFilePath = "D:\\Software_Lab\\Software_Lab3\\textfile\\easy_test.txt";
        app.initForTest(testFilePath);
    }
    @Test
    public void test_beginning_BridgeWords() {
        String result = app.queryBridgeWords("the", "carefully");
        assertEquals("The bridge words from the to carefully are: scientist.", result);
    }

    @Test
    public void test_ending_BridgeWords() {
        String result = app.queryBridgeWords("analyzed", "again");
        assertEquals("The bridge words from analyzed to again are: it.", result);
    }

    @Test
    public void test_beginning_NoBridgeWords() {
        String result = app.queryBridgeWords("scientist", "data");
        assertEquals("No bridge words from scientist to data!", result);
    }

    @Test
    public void test_ending_NoBridgeWords() {
        String result = app.queryBridgeWords("so", "it");
        assertEquals("No bridge words from so to it!", result);
    }

    @Test
    public void test_wrongorder_input() {
        String result = app.queryBridgeWords("data", "requested");
        assertEquals("No bridge words from data to requested!", result);
    }

    @Test
    public void testEmptyInput() {
        String result = app.queryBridgeWords("", "team");
        assertEquals("Input Error!", result);
    }
}