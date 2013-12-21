package norg.javiki.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public abstract class XmlSaxParseInterface<Result> extends DefaultHandler implements ParseInterface<Result> {
	private static final String TAG = "XmlSaxParseInterface";
	private Result mResult;

	final public Result parse(InputStream is, String encode) {
		SAXParserFactory saxPF = SAXParserFactory.newInstance();
	    try {
	    	SAXParser saxP = saxPF.newSAXParser();
	    	XMLReader xmlR = saxP.getXMLReader();
	    	xmlR.setContentHandler(this);
	    	InputSource input = new InputSource(is);
	    	input.setEncoding(encode);
	    	onInitVariable(mResult);
			xmlR.parse(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Result result = null;
//		try {
//			for (int evtType = xmlParser.getEventType(); evtType != XmlPullParser.END_DOCUMENT; evtType = xmlParser.next()) {
//				parse(xmlParser, evtType, result);
//			}
//		} catch (XmlPullParserException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return result;
		
		return null;

	}
	
	protected void startDocument(Result result){}
	protected void endDocument(Result result){}
	
	protected abstract void onInitVariable(Result result);
	protected abstract void startTag(Result result, int depth, String tagName, Attributes attributes);
	protected abstract void endTag(Result result, int depth, String tagName);
	protected abstract void text(Result result, int depth, String tagName);
	
	String mTagName;
	String mNamespaceUri;
	String mTagNameShort;
	int mDepth = -1;
	
	protected final String getTagName() {
		return mTagName;
		
	}
	protected final String getNamespaceUrl() {
		return mNamespaceUri;
		
	}
	protected final String getLocalName() {
		return mTagNameShort;
		
	}
	
	private boolean mIsPrinterOn;
	protected void setPrinterOn(boolean isPrinterOn) {
		mIsPrinterOn = isPrinterOn;
	}
//	protected void parse(XmlPullParser xmlParser, int evtType, Result result) throws XmlPullParserException, IOException{}
	
	@Override
	public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		++mDepth;
		mNamespaceUri = uri;
		mTagNameShort = localName;
		mTagName = qName;
		if (mIsPrinterOn) printStartElement(qName, attributes);
		startTag(mResult, mDepth, mTagName, attributes);
	}
	
	@Override
	public final void endElement(String uri, String localName, String qName) throws SAXException {
		--mDepth;
		if (mIsPrinterOn) printEndElement(qName);
	}

	@Override
	public final void characters(char[] ch, int start, int length) throws SAXException {
		String  url = String.valueOf(ch, start, length);
		text(mResult, mDepth, mTagName);
		if (mIsPrinterOn) printCharacters(mTagName, url);
	}
	
	private void printCharacters(String mTagName2, String url) {
		// TODO Auto-generated method stub
		
	}

	private void printEndElement(String qName) {
		// TODO Auto-generated method stub
		
	}

	private void printStartElement(String qName, Attributes attributes) {
		Log.d(TAG, "START_TAG: " + qName);
		for (int i = 0; i < attributes.getLength(); ++i) {
			Log.d(TAG, "TAG_ATTRI(" + i + "): " + attributes.getQName(i) + "="
					+ attributes.getValue(i));
		}
	}
	
    public final void startDocument () throws SAXException {
    	++mDepth;
    }

    public final void endDocument () throws SAXException {
    	--mDepth;
    	mNamespaceUri = null;
    	mTagNameShort = null;
    	mTagName = null;
    }
//	DefaultHandler mSAXHandler = new DefaultHandler() {
////	    Boolean elementOn = false;
//
//	};

}
