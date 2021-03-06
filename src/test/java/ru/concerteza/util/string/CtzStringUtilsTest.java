package ru.concerteza.util.string;

import org.junit.Assert;
import org.junit.Test;
import ru.concerteza.util.string.CtzStringUtils;

/**
 * User: alexey
 * Date: 4/2/12
 */
public class CtzStringUtilsTest {

    @Test
    public void test() {
        String input = "<foo><bar/></foo>";
        String pretty = CtzStringUtils.prettifyXml(input);
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<foo>\n  <bar/>\n</foo>\n", pretty);
    }
}
