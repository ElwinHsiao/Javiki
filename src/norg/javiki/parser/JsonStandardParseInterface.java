package norg.javiki.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONObject;

import android.util.Log;

public abstract class JsonStandardParseInterface<Result> implements ParseInterface<Result> {
	private static final String TAG = "JsonParseInterface";
	final public Result parse(InputStream is, String encode) {
		if (encode == null) encode = DEFAULT_ENCODE;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(is, encode), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			String json = sb.toString();
			JSONObject jObj = new JSONObject(json);
			return parse(jObj);
		} catch (Exception e) {
			Log.e(TAG, "input stream error: " + e);
		}
		
		return null;
	}
	protected abstract Result parse(JSONObject jObj);
}
