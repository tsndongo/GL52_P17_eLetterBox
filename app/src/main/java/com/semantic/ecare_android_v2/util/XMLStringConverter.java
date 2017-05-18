package com.semantic.ecare_android_v2.util;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.util.Log;


public class XMLStringConverter {
	
	private String CLASSNAME=this.getClass().getName();
	
	public String convertDocumentToString(Document doc)
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try
		{
			transformer = tf.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			String output = writer.getBuffer().toString();
			return output;
		}
		catch (TransformerException e)
		{
			Log.e(Constants.TAG, CLASSNAME + e.getMessage());
		}
		return null;
	}
	
	public Document convertStringToDocument(String xmlStr)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try
		{
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		}
		catch (Exception e)
		{
			Log.e(Constants.TAG, CLASSNAME + e.getMessage());
		}
		return null;
	}
	
}
