package com.rhinoforms.formparser;

import static com.rhinoforms.TestUtil.createDocument;
import static com.rhinoforms.TestUtil.serialiseHtmlCleanerNode;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Properties;

import junit.framework.Assert;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.rhinoforms.Constants;
import com.rhinoforms.TestApplicationContext;
import com.rhinoforms.util.StreamUtils;

public class ValueInjectorTest {

    private ValueInjector valueInjector;
    private HtmlCleaner htmlCleaner;
    private TagNode formHtml;
    private Document dataDocument;
    private Properties properties;
    private StreamUtils streamUtils;

    @Before
    public void setup() throws Exception {
        TestApplicationContext applicationContext = new TestApplicationContext();
        htmlCleaner = applicationContext.getHtmlCleaner();
        formHtml = htmlCleaner.clean(new FileInputStream("src/test/resources/fishes.html"));
        dataDocument = createDocument("<myData><ocean><name>Pacific</name><fishes><fish><name>One</name></fish><fish><name>Two</name></fish></fishes></ocean></myData>");

        this.valueInjector = applicationContext.getValueInjector();
        properties = new Properties();
        streamUtils = new StreamUtils();
    }

    @Test
    public void testForEachWithDocBaseRelativeXPath() throws Exception {
        valueInjector.processForEachStatements(properties, formHtml, dataDocument, "/myData/ocean");

        String actual = serialiseHtmlCleanerNode(formHtml);
        Assert.assertFalse(actual.contains("foreach"));
        Assert.assertTrue(actual.contains("<span index=\"1\">One</span>"));
        Assert.assertTrue(actual.contains("<span index=\"2\">Two</span>"));
    }

    @Test
    public void testForEachWithDocBaseRelativeXPathTextNodes() throws Exception {
        formHtml = htmlCleaner.clean(new FileInputStream("src/test/resources/fishes-text-nodes.html"));
        dataDocument = createDocument("<myData><fishes><fish_one>One</fish_one><fish_two>Two</fish_two><fish_three/></fishes></myData>");
        valueInjector.processForEachStatements(properties, formHtml, dataDocument, "/myData");

        String actual = serialiseHtmlCleanerNode(formHtml);

        System.out.println(actual);

        Assert.assertFalse(actual.contains("foreach"));
        Assert.assertTrue(actual.contains("<span>One</span>"));
        Assert.assertTrue(actual.contains("<span>Two</span>"));
        // Disabled for windows due to unpredicable results
        if (!System.getProperty("os.name").startsWith("Windows")) {
            String expected = "<html><head></head><body><div class=\"before\">" + Constants.NEW_LINE
                    + " <span>One</span>" + Constants.NEW_LINE
                    + " <span>Two</span>" + Constants.NEW_LINE
                    + " <span>{{aFish}}</span>" + Constants.NEW_LINE
                    + "<div class=\"after\"></div></div></body></html>";
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testForEachWithAbsoluteXPath() throws Exception {
        formHtml = htmlCleaner.clean(new FileInputStream("src/test/resources/forEach-absolute-xpath.html"));
        TagNode forEachNode = formHtml.findElementByName("rf.forEach", true);
        String selectStatement = forEachNode.getAttributeByName("select");
        Assert.assertEquals("Select statement is absolute XPath", "/myData/ocean/fishes/fish", selectStatement);

        valueInjector.processForEachStatements(properties, formHtml, dataDocument, "/myData");

        String actual = serialiseHtmlCleanerNode(formHtml);
        Assert.assertFalse(actual.contains("foreach"));
        Assert.assertTrue(actual.contains("<span index=\"1\">One</span>"));
        Assert.assertTrue(actual.contains("<span index=\"2\">Two</span>"));
    }

    @Test
    public void testForEachWithSearchXPath() throws Exception {
        formHtml = htmlCleaner.clean(new FileInputStream("src/test/resources/forEach-search-xpath.html"));
        TagNode forEachNode = formHtml.findElementByName("rf.forEach", true);
        String selectStatement = forEachNode.getAttributeByName("select");
        Assert.assertEquals("Select statement is search XPath", "//fish", selectStatement);

        valueInjector.processForEachStatements(properties, formHtml, dataDocument, "/myData");

        String actual = serialiseHtmlCleanerNode(formHtml);
        Assert.assertFalse(actual.contains("foreach"));
        Assert.assertTrue(actual.contains("<span index=\"1\">One</span>"));
        Assert.assertTrue(actual.contains("<span index=\"2\">Two</span>"));
    }

    @Test
    public void testProcessRemainingCurlyBrackets() throws Exception {
        Assert.assertTrue("Placeholder present.", serialiseHtmlCleanerNode(formHtml).contains("<span class=\"ocean\">{{name}}</span>"));

        valueInjector.processCurlyBrackets(dataDocument, formHtml, properties, "/myData/ocean");

        String actual = serialiseHtmlCleanerNode(formHtml);
        Assert.assertTrue("Placeholder replaced with dataDocument content.", actual.contains("<span class=\"ocean\">Pacific</span>"));
        Assert.assertTrue(actual.contains("Fish count: 2"));
        Assert.assertTrue(actual.contains("First fish: One"));
    }

    @Test
    public void testProcessRemainingCurlyBracketsAttributeXPath() throws Exception {
        dataDocument = createDocument("<myData><fishes><fish name='1'>One</fish><fish name='2'>Two</fish></fishes></myData>");
        formHtml = htmlCleaner.clean(new StringReader("<span>{{//*[@name='2']}}</span>"));

        valueInjector.processCurlyBrackets(dataDocument, formHtml, properties, "/myData");

        String actual = serialiseHtmlCleanerNode(formHtml);
        Assert.assertTrue("Placeholder replaced with dataDocument content.", actual.contains("<span>Two</span>"));
    }

    @Test
    public void testProcessRemainingCurlyBracketsSomeProperties() throws Exception {
        formHtml = htmlCleaner.clean(new StringReader("<span class=\"ocean\">{{$someUrl}}{{name}}</span>"));
        Assert.assertTrue("Placeholder present.", serialiseHtmlCleanerNode(formHtml).contains("<span class=\"ocean\">{{$someUrl}}{{name}}</span>"));
        properties.put("someUrl", "http://en.wikipedia.org/wiki/");

        valueInjector.processCurlyBrackets(dataDocument, formHtml, properties, "/myData/ocean");

        String actual = serialiseHtmlCleanerNode(formHtml);
        Assert.assertTrue("Placeholder replaced with dataDocument content.", actual.contains("<span class=\"ocean\">http://en.wikipedia.org/wiki/Pacific</span>"));
    }

    @Test
    public void testStringBuilderToNode() throws Exception {
        String html = "<div><span>one</span><span>two</span></div>";
        TagNode node = valueInjector.stringBuilderToNode(new StringBuilder(html));
        String actual = serialiseHtmlCleanerNode(node);
        Assert.assertEquals(html, actual);
    }

    @Test
    public void testStringBuilderBodyToNode() throws Exception {
        String html = "<body><span>one</span><span>two</span></body>";
        TagNode node = valueInjector.stringBuilderBodyToNode(new StringBuilder(html));
        String actual = serialiseHtmlCleanerNode(node);
        Assert.assertEquals(html, actual);
    }

    @Test
    public void testNodeToStringBuilder() throws Exception {
        String html = "<div><span>one</span><span>two</span></div>";
        TagNode node = valueInjector.stringBuilderToNode(new StringBuilder(html));
        StringBuilder stringBuilder = valueInjector.nodeToStringBuilder(node);
        Assert.assertEquals(html, stringBuilder.toString());
    }

    @Test
    public void testProcessFlowDefinitionCurlyBrackets() throws Exception {
        String initialFlowDef = "{ docBase: '{{$baseNode}}', formLists: { main: [ { id: 'customer', url: '/forms/simplest/{{$firstForm}}', actions: [ 'finish' ] } ] } }";
        String expectedFlowDef = "{ docBase: '/myDocBase', formLists: { main: [ { id: 'customer', url: '/forms/simplest/simplest.html', actions: [ 'finish' ] } ] } }";
        StringBuilder flowStringBuilder = new StringBuilder(initialFlowDef);
        Properties flowProperties = new Properties();
        flowProperties.setProperty("baseNode", "/myDocBase");
        flowProperties.setProperty("firstForm", "simplest.html");

        Assert.assertEquals(initialFlowDef, flowStringBuilder.toString());

        valueInjector.processFlowDefinitionCurlyBrackets(flowStringBuilder, flowProperties);

        Assert.assertEquals(expectedFlowDef, flowStringBuilder.toString());
    }

    @Test
    public void testNestedForLoopsOneLevelNesting() throws Exception {
        dataDocument = createDocument("<mydata><policy> <name>Motor</name> <vehicles>  <vehicle><name>Ford Capri</name></vehicle>  <vehicle><name>AC Cobra</name></vehicle>  <vehicle><name>Jaguar E-type</name></vehicle> </vehicles> <drivers>  <driver><name>Mickey Mouse</name></driver>  <driver><name>Fred Flintstone</name></driver>  <driver><name>Super Ted</name></driver> </drivers></policy></mydata>");

        formHtml = htmlCleaner.clean(getClass().getResourceAsStream("nested-for-each-source.html"));
        String expectedHtml = new String(streamUtils.readStream(getClass().getResourceAsStream("nested-for-each-expected.html")));

        // Assert not true
        Assert.assertFalse(expectedHtml.equals(serialiseHtmlCleanerNode(formHtml)));

        // Do something
        valueInjector.processForEachStatements(null, formHtml, dataDocument, "/mydata");

        // Assert true
        Assert.assertEquals(expectedHtml, serialiseHtmlCleanerNode(formHtml));
    }

    @Test
    public void testNestedForLoopsOneLevelNestingWithNestedIteration() throws Exception {
        dataDocument = createDocument("<myData><ocean><name>Atlantic</name><fishes><fish><name>A One</name></fish><fish><name>A Two</name></fish></fishes></ocean><ocean><name>Pacific</name><fishes><fish><name>P One</name></fish><fish><name>P Two</name></fish><fish><name>P Three</name></fish></fishes></ocean></myData>");

        formHtml = htmlCleaner.clean(getClass().getResourceAsStream("nested-for-each-with-nested-iteration-source.html"));
        String expectedHtml = new String(streamUtils.readStream(getClass().getResourceAsStream("nested-for-each-with-nested-iteration-expected.html")));

        // Assert not true
        Assert.assertFalse(expectedHtml.equals(serialiseHtmlCleanerNode(formHtml)));

        // Do something
        valueInjector.processForEachStatements(null, formHtml, dataDocument, "/myData");

        // Assert true
        Assert.assertEquals(expectedHtml, serialiseHtmlCleanerNode(formHtml));
    }

}
