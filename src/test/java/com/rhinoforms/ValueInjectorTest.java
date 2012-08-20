package com.rhinoforms;

import static com.rhinoforms.TestUtil.createDocument;
import static com.rhinoforms.TestUtil.serialiseNode;

import java.io.FileInputStream;

import junit.framework.Assert;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class ValueInjectorTest {

	private ValueInjector valueInjector;
	private HtmlCleaner htmlCleaner;
	private TagNode formHtml;
	private Document dataDocument;

	@Before
	public void setup() throws Exception {
		htmlCleaner = new HtmlCleaner();
		formHtml = htmlCleaner.clean(new FileInputStream("src/test/resources/fishes.html"));
		dataDocument = createDocument("<myData><ocean><name>Pacific</name><fishes><fish><name>One</name></fish><fish><name>Two</name></fish></fishes></ocean></myData>");

		this.valueInjector = new ValueInjector();
	}

	@Test
	public void testRecuringEntityOutput() throws Exception {

		valueInjector.processForEachStatements(formHtml, dataDocument, "/myData/ocean");

		String actual = serialiseNode(formHtml);
		Assert.assertTrue(actual.contains("<span index=\"1\">One</span>"));
		Assert.assertTrue(actual.contains("<span index=\"2\">Two</span>"));
	}

	@Test
	public void testProcessRemainingCurlyBrackets() throws Exception {
		Assert.assertTrue("Placeholder present.", serialiseNode(formHtml).contains("<span class=\"ocean\">{name}</span>"));

		valueInjector.processRemainingCurlyBrackets(formHtml, dataDocument, "/myData/ocean");

		String serialiseNode = serialiseNode(formHtml);
		
		Assert.assertTrue("Placeholder replaced with dataDocument content.",
				serialiseNode.contains("<span class=\"ocean\">Pacific</span>"));
	}

}