package norg.javiki.parser;

import java.util.Arrays;

import norg.javiki.ClassName;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

public class XmlUtils {
	private static final String TAG = ClassName.CALLED_CLASS_NAME();

	public static void printXml(XmlPullParser xmlParser, int evtType) {
		char[] marginChar = new char[xmlParser.getDepth()*2];
		Arrays.fill(marginChar, ' ');
		String margin = String.copyValueOf(marginChar);
//		for (int i = 0; i < xmlParser.getDepth(); ++i) {
//			margin += "  ";
//		}
		if (evtType == XmlPullParser.TEXT) {
			String text = xmlParser.getText().trim().matches(".*[^/t/n/r/f/x0B ]+.*") ? ("("+xmlParser.getText()+")") : "(null)";
			
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
}
