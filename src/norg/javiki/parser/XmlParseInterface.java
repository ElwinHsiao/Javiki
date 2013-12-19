package norg.javiki.parser;

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
		
		return parse(xmlParser);
	}
	
	protected abstract Result parse(XmlPullParser parser);
}
