package com.rhinoforms.flow;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.rhinoforms.TestConnectionFactory;
import com.rhinoforms.TestResourceLoader;
import com.rhinoforms.TestUtil;
import com.rhinoforms.formparser.ValueInjector;
import com.rhinoforms.resourceloader.ResourceLoaderImpl;
import com.rhinoforms.xml.DocumentHelper;

public class RemoteSubmissionHelperTest {
	
	private RemoteSubmissionHelper remoteSubmissionHelper;
	private Document dataDocument;
	private DocumentHelper documentHelper;
	private TestConnectionFactory testConnectionFactory;
	private String dataDocumentString;
	private Map<String, String> xsltParameters;
	private FormFlow formFlow;
	
	@Before
	public void setup() throws Exception {
		documentHelper = new DocumentHelper();
		ResourceLoaderImpl resourceLoader = new ResourceLoaderImpl(new TestResourceLoader(), new TestResourceLoader());
		remoteSubmissionHelper = new RemoteSubmissionHelper(resourceLoader, new ValueInjector());
		testConnectionFactory = new TestConnectionFactory();
		remoteSubmissionHelper.setConnectionFactory(testConnectionFactory);
		dataDocumentString = "<myData><something>a</something><another>anotherVal</another></myData>";
		dataDocument = TestUtil.createDocument(dataDocumentString);
		xsltParameters = new HashMap<String, String>();
		formFlow = new FormFlow();
		formFlow.setDataDocument(dataDocument);
	}
	
	@Test
	public void testSimplestHandleSubmissionPOST() throws Exception {
		String requestUrl = "http://localhost/dummyURL";
		Submission submission = new Submission(requestUrl);
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("tree", "apple tree");
		submission.getData().put("fruit", "apple");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals(requestUrl, testConnectionFactory.getRecordedRequestUrl());
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("fruit=apple&tree=apple tree", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testSimplestHandleSubmissionGET() throws Exception {
		String requestUrl = "http://localhost/dummyURL";
		Submission submission = new Submission(requestUrl);
		submission.setMethod("get");
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("something", "xpath://something");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("http://localhost/dummyURL?something=a", testConnectionFactory.getRecordedRequestUrl());
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testSimplestHandleSubmissionGETRestStyleUrlWithXpath() throws Exception {
		String requestUrl = "http://localhost/dummyURL/{{//something}}-{{/myData/another}}";
		Submission submission = new Submission(requestUrl);
		submission.setMethod("get");
		submission.setOmitXmlDeclaration(true);
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("http://localhost/dummyURL/a-anotherVal", testConnectionFactory.getRecordedRequestUrl());
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}

	@Test
	public void testSimplestHandleSubmissionGETRestStyleUrlWithFlowProperty() throws Exception {
		String requestUrl = "http://localhost/dummyURL/{{$myProp}}";
		Submission submission = new Submission(requestUrl);
		submission.setMethod("get");
		submission.setOmitXmlDeclaration(true);
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		Properties properties = new Properties();
		properties.put("myProp", "prop-value-123");
		formFlow.setProperties(properties);
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("http://localhost/dummyURL/prop-value-123", testConnectionFactory.getRecordedRequestUrl());
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}

	@Test
	public void testHandleSubmissionWithDataDoc() throws Exception {
		String requestUrl = "http://localhost/dummyURL";
		Submission submission = new Submission(requestUrl);
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("xml", "[dataDocument]");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals(requestUrl, testConnectionFactory.getRecordedRequestUrl());
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("xml=" + dataDocumentString, URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionXPathParam() throws Exception {
		String requestUrl = "http://localhost/dummyURL";
		Submission submission = new Submission(requestUrl);
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("something", "xpath://something");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals(requestUrl, testConnectionFactory.getRecordedRequestUrl());
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("something=a", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionXPathParamMultipleValues() throws Exception {
		String requestUrl = "http://localhost/dummyURL";
		Submission submission = new Submission(requestUrl);
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("something", "xpath://something");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		String dataDocString = "<myData><something>a</something><something>b</something><something>c</something></myData>";
		dataDocument = TestUtil.createDocument(dataDocString);
		formFlow.setDataDocument(dataDocument);
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals(requestUrl, testConnectionFactory.getRecordedRequestUrl());
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("something=a&something=b&something=c", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionXPathParamsOneBlank() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("something", "xpath://something");
		submission.getData().put("myData", "xpath:/myData");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("myData=&something=a", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionWithDataDocWithXmlDeclaration() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.getData().put("xml", "[dataDocument]");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("xml=<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + dataDocumentString, URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleRawSubmissionWithXmlDeclaration() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setRawXmlRequest(true);
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/xml", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + dataDocumentString, submittedData);
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals(dataDocumentString, dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionInsertResult() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("xml", "[dataDocument]");
		submission.setResultInsertPoint("/myData/submissionResult");
		testConnectionFactory.setResponseString("<data>one</data>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("xml=" + dataDocumentString, URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals("<myData><something>a</something><another>anotherVal</another><submissionResult><data>one</data></submissionResult></myData>", dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionInsertResultReplaceExisting() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("xml", "[dataDocument]");
		submission.setResultInsertPoint("/myData/submissionResult");
		testConnectionFactory.setResponseString("<data>one</data>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("xml=" + dataDocumentString, URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals("<myData><something>a</something><another>anotherVal</another><submissionResult><data>one</data></submissionResult></myData>", dataDocumentStringAfterSubmission);
		
		testConnectionFactory.resetRecordedRequestStream();
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals("<myData><something>a</something><another>anotherVal</another><submissionResult><data>one</data></submissionResult></myData>", dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionBadResult() {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("xml", "[dataDocument]");
		submission.setResultInsertPoint("/myData");
		testConnectionFactory.setResponseString("<submissionResult>one</submissionResult>");
		testConnectionFactory.setResponseCode(500);
		testConnectionFactory.setResponseMessage("Something went very wrong.");
		
		try {
			remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
			Assert.fail("Should have thrown Exception");
		} catch (RemoteSubmissionHelperException e) {
			// Pass
			Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
			Assert.assertEquals("Bad response from target service. Status:500, message:Something went very wrong.", e.getMessage());
		}
	}
	
	@Test
	public void testHandleSubmissionPreTransform() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setOmitXmlDeclaration(true);
		submission.setPreTransform("xslt/toServerFormat.xsl");
		submission.getData().put("xml", "[dataDocument]");
		submission.setResultInsertPoint("/myData/submissionResult");
		testConnectionFactory.setResponseString("<data>one</data>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("xml=<serverData><abc>a</abc></serverData>", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals("<myData><something>a</something><another>anotherVal</another><submissionResult><data>one</data></submissionResult></myData>", dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionPreTransformWithXmlDeclaration() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setPreTransform("xslt/toServerFormat.xsl");
		submission.getData().put("xml", "[dataDocument]");
		submission.setResultInsertPoint("/myData/submissionResult");
		testConnectionFactory.setResponseString("<data>one</data>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("xml=<?xml version=\"1.0\" encoding=\"UTF-8\"?><serverData><abc>a</abc></serverData>", URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals("<myData><something>a</something><another>anotherVal</another><submissionResult><data>one</data></submissionResult></myData>", dataDocumentStringAfterSubmission);
	}
	
	@Test
	public void testHandleSubmissionPostTransform() throws Exception {
		Submission submission = new Submission("http://localhost/dummyURL");
		submission.setOmitXmlDeclaration(true);
		submission.getData().put("myXml", "[dataDocument]");
		submission.setPostTransform("xslt/fromServerFormat.xsl");
		submission.setResultInsertPoint("/myData/submissionResult");
		testConnectionFactory.setResponseString("<serverData><wrapper><premium123>10.00</premium123></wrapper></serverData>");
		
		remoteSubmissionHelper.handleSubmission(submission, xsltParameters, formFlow);
		
		Assert.assertEquals("application/x-www-form-urlencoded", testConnectionFactory.getRecordedRequestProperties().get("Content-Type"));
		String submittedData = new String(testConnectionFactory.getRecordedRequestStream().toByteArray());
		Assert.assertEquals("myXml=" + dataDocumentString, URLDecoder.decode(submittedData, "UTF-8"));
		String dataDocumentStringAfterSubmission = documentHelper.documentToString(dataDocument);
		Assert.assertEquals("<myData><something>a</something><another>anotherVal</another><submissionResult><submissionResult><premium>10.00</premium></submissionResult></submissionResult></myData>", dataDocumentStringAfterSubmission);
	}
	
}
