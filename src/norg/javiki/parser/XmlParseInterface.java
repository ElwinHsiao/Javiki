package norg.javiki.parser;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public abstract class XmlParseInterface<Result> implements ParseInterface<Result> {
	private static final String TAG = "XmlParseInterface";

	final public Result parse(InputStream is, String encode) {
		if (encode == null) encode = DEFAULT_ENCODE;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(is, encode);
		} catch (XmlPullParserException e) {
			Log.e(TAG, "input stream error: " + e);
			return null;
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
		
		return parse(xmlParser);
	}
	
	protected abstract Result parse(XmlPullParser parser);
//	protected void parse(XmlPullParser xmlParser, int evtType, Result result) throws XmlPullParserException, IOException{}
}
