package norg.javiki.parser;

import java.io.IOException;
import java.util.Arrays;

import norg.javiki.ClassName;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class XmlUtils {
	private static final String TAG = ClassName.CALLED_CLASS_NAME();

	public static void printXml(XmlPullParser xmlParser, int evtType) {
		char[] marginChar = new char[xmlParser.getDepth()*2];
		Arrays.fill(marginChar, ' ');
		String margin = String.copyValueOf(marginChar);
		
		if (evtType == XmlPullParser.TEXT) {
			String text = xmlParser.getText().trim().matches("[\\s]*") ? "(<null>)" : ("("+xmlParser.getText()+")");
			
			Log.d(TAG, "parse xml: " + margin + "--depth=" + xmlParser.getDepth()
					+ " evtType=TEXT" + text);
		} else {
			Log.d(TAG, "parse xml: " + margin + "depth=" + xmlParser.getDepth()
					 + " tagName=" + xmlParser.getName()+ " evtType=" + XmlPullParser.TYPES[evtType]);
		}

		for (int i = 0; i < xmlParser.getAttributeCount(); ++i) {
			Log.d(TAG, "parse xml: " + margin + "-AttrValue(" + i + ")=" + xmlParser.getAttributeValue(i));
		}
	}
	
	public static void printXmlTree(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		for (int evtType = xmlParser.getEventType(); evtType != XmlPullParser.END_DOCUMENT; evtType = xmlParser.next()) {
			printXml(xmlParser, evtType);
		}
	}
}
