package norg.javiki.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

public abstract class GeneralgXmlParser<Result> extends AsyncTask<String, Integer, Result> {

	private static final String TAG = "GeneralgXmlParser";
	private String url;
	public void parse(String url) {
		this.url = url;
		execute(url);
	}
	
	public String getUrl() {
		return url;
	}

	protected abstract Result parseAlgorithm(XmlPullParser parser);
	protected abstract void onParseFinished(Result result);

	@Override
	final protected Result doInBackground(String... params) {
		// TODO
		InputStream is = openInputStream(url);
		if (is == null) {
			Log.e(TAG, "get inputStream fail!");
			return null;
		}
		
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(is, "utf-8");
		} catch (XmlPullParserException e) {
			Log.e(TAG, "input stream error: " + e);
			return null;
		}
		
		return parseAlgorithm(xmlParser);
	}

	@Override
	final protected void onPostExecute(Result result) {
		onParseFinished(result);
	}
	private InputStream openInputStream(String url) {
		try {
			URL mUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
			conn.setConnectTimeout(5);
			conn.setReadTimeout(5);
			InputStream is = conn.getInputStream();
			return is;
//        conn.setInstanceFollowRedirects(false);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
//	public interface OnParseListener<Result> {
//		Result parseAlgorithm(String url, XmlPullParser parser);
//		void onParseFinished(String url, Result result);
//	}
	
//	public interface ParseStrategy<Result> {
//		Result execute(XmlPullParser parser);
//	}
	
//	class ParseTask extends AsyncTask<String, Integer, Result> {
//
//		@Override
//		protected Result doInBackground(String... params) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Result result) {
//
//		}
//	}
}
